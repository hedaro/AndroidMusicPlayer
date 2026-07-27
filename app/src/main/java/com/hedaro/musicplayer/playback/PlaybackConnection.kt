package com.hedaro.musicplayer.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.hedaro.musicplayer.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The UI's single handle to playback. Connects a Media3 [MediaController] to [PlaybackService],
 * exposes a [PlaybackState] StateFlow the UI observes, and offers transport controls. The UI never
 * touches ExoPlayer or the service directly — only this class.
 */
@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var controller: MediaController? = null

    // Identifies what produced the current queue (e.g. "album=12", "library|sort=TITLE|q=").
    // Lets a tap on a track from the same source jump within the queue instead of rebuilding it.
    // In-memory only: after a process kill the queue is restored but this is null, so the first
    // tap rebuilds.
    private var currentSource: String? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get().apply { addListener(playerListener) }
                updateState()
                startPositionUpdates()
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    // --- Transport controls -------------------------------------------------

    /**
     * Play [tracks] (from list context [source]) starting at [startIndex]. If [source] matches the
     * queue that's already playing and the tapped track is still in it, just jump to it; otherwise
     * rebuild the queue from [tracks]. This keeps "tap a song in the list I'm already playing" a
     * cheap jump, while tapping a song from a different list (album, playlist, filter…) replaces
     * the queue with that list.
     */
    fun playFrom(source: String, tracks: List<Track>, startIndex: Int) {
        val c = controller ?: return
        val target = tracks.getOrNull(startIndex) ?: return
        if (source == currentSource) {
            val queueIndex = c.indexOfTrack(target.id)
            if (queueIndex >= 0) {
                c.seekTo(queueIndex, 0L)
                c.play()
                return
            }
        }
        currentSource = source
        playTracks(tracks, startIndex)
    }

    /** Replace the queue with [tracks] and start playing from [startIndex]. */
    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        val c = controller ?: return
        if (tracks.isEmpty()) return
        c.shuffleModeEnabled = false
        c.setMediaItems(tracks.map { it.toMediaItem() }, startIndex.coerceIn(0, tracks.lastIndex), 0L)
        c.prepare()
        c.play()
    }

    /**
     * Shuffle-play entry point: start the whole [tracks] list (from list context [source]) playing
     * in random order. Enables shuffle mode and begins from a random track.
     */
    fun shufflePlay(source: String, tracks: List<Track>) {
        val c = controller ?: return
        if (tracks.isEmpty()) return
        currentSource = source
        c.shuffleModeEnabled = true
        val startIndex = Random.nextInt(tracks.size)
        c.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0L)
        c.prepare()
        c.play()
    }

    /** Index of the first queue item backing [trackId], or -1 if it isn't in the queue. */
    private fun MediaController.indexOfTrack(trackId: Long): Int {
        val idStr = trackId.toString()
        for (i in 0 until mediaItemCount) {
            if (getMediaItemAt(i).mediaId == idStr) return i
        }
        return -1
    }

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun play() { controller?.play() }
    fun pause() { controller?.pause() }
    fun stop() { controller?.stop() }
    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }

    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs.coerceAtLeast(0L)) }

    // --- Queue controls -----------------------------------------------------

    /**
     * Insert [track] right after the current item so it plays next. If nothing is queued yet,
     * start it playing straight away (there's no "current" track for it to follow).
     */
    fun playNext(track: Track) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            c.setMediaItems(listOf(track.toMediaItem()))
            c.prepare()
            c.play()
        } else {
            c.addMediaItem(c.currentMediaItemIndex + 1, track.toMediaItem())
        }
    }

    /** Append [track] to the end of the queue. Starts a fresh queue if empty. */
    fun addToQueue(track: Track) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            c.setMediaItems(listOf(track.toMediaItem()))
            c.prepare()
        } else {
            c.addMediaItem(track.toMediaItem())
        }
    }

    /** Jump to (and start) the queue item at [index]. */
    fun jumpTo(index: Int) { controller?.seekTo(index, 0L) }

    /** Reorder the queue: move the item at [from] to [to]. */
    fun moveInQueue(from: Int, to: Int) { controller?.moveMediaItem(from, to) }

    /** Remove the queue item at [index]. */
    fun removeFromQueue(index: Int) { controller?.removeMediaItem(index) }

    /** Empty the whole queue and stop playback. */
    fun clearQueue() {
        controller?.clearMediaItems()
        currentSource = null
    }

    /** Step by a signed offset (e.g. +5s / -10s), clamped to the track bounds. */
    fun stepBy(deltaMs: Long) {
        val c = controller ?: return
        val duration = c.duration.coerceAtLeast(0L)
        val upperBound = if (duration > 0L) duration else Long.MAX_VALUE
        c.seekTo((c.currentPosition + deltaMs).coerceIn(0L, upperBound))
    }

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    /** Cycle OFF -> ALL -> ONE -> OFF. */
    fun cycleRepeatMode() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun release() {
        controller?.apply {
            removeListener(playerListener)
            release()
        }
        controller = null
        scope.cancel()
    }

    // --- State plumbing -----------------------------------------------------

    private val playerListener = object : Player.Listener {
        // Recompute the whole snapshot on any relevant change — simple and correct.
        override fun onEvents(player: Player, events: Player.Events) = updateState()
    }

    /** Position isn't event-driven, so poll it a couple of times a second while connected. */
    private fun startPositionUpdates() {
        scope.launch {
            while (isActive) {
                controller?.let { c ->
                    _playbackState.update {
                        it.copy(
                            positionMs = c.currentPosition.coerceAtLeast(0L),
                            bufferedPositionMs = c.bufferedPosition.coerceAtLeast(0L),
                        )
                    }
                }
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun updateState() {
        val c = controller
        if (c == null) {
            _playbackState.value = PlaybackState(isConnected = false)
            return
        }
        val metadata = c.mediaMetadata
        val queue = buildList {
            // The same track can appear more than once, so disambiguate by occurrence for a
            // stable, unique list key per position.
            val occurrences = HashMap<Long, Int>()
            for (i in 0 until c.mediaItemCount) {
                val item = c.getMediaItemAt(i)
                val itemMetadata = item.mediaMetadata
                val trackId = item.mediaId.toLongOrNull() ?: -1L
                val occurrence = occurrences.getOrDefault(trackId, 0)
                occurrences[trackId] = occurrence + 1
                add(
                    QueueItem(
                        id = trackId,
                        key = "$trackId#$occurrence",
                        title = itemMetadata.title?.toString().orEmpty(),
                        artist = itemMetadata.artist?.toString().orEmpty(),
                        artworkUri = itemMetadata.artworkUri,
                    ),
                )
            }
        }
        _playbackState.update {
            it.copy(
                isConnected = true,
                currentTrackId = c.currentMediaItem?.mediaId?.toLongOrNull(),
                title = metadata.title?.toString().orEmpty(),
                artist = metadata.artist?.toString().orEmpty(),
                album = metadata.albumTitle?.toString().orEmpty(),
                artworkUri = metadata.artworkUri,
                isPlaying = c.isPlaying,
                durationMs = c.duration.coerceAtLeast(0L),
                shuffleEnabled = c.shuffleModeEnabled,
                repeatMode = c.repeatMode.toRepeatMode(),
                hasNext = c.hasNextMediaItem(),
                hasPrevious = c.hasPreviousMediaItem(),
                queueSize = c.mediaItemCount,
                queue = queue,
                currentIndex = c.currentMediaItemIndex,
            )
        }
    }

    private fun Int.toRepeatMode(): RepeatMode = when (this) {
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.OFF
    }

    private companion object {
        const val POSITION_UPDATE_INTERVAL_MS = 500L
    }
}
