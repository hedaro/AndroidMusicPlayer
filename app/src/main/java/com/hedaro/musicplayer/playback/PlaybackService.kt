package com.hedaro.musicplayer.playback

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.hedaro.musicplayer.data.repository.MusicRepository
import com.hedaro.musicplayer.data.repository.QueuePersistenceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background playback service built on Media3.
 *
 * Owns the [ExoPlayer] and a [MediaSession]; the UI drives playback through a `MediaController`
 * (see [PlaybackConnection]). Because playback lives here, it survives the UI being backgrounded and
 * Media3 provides the media notification + lock-screen controls automatically.
 *
 * Hosts two background rules:
 *  - **Play count:** a track counts once it passes [PLAY_COUNT_THRESHOLD_MS], at most once per play.
 *  - **Queue persistence:** the queue + playback position are saved to disk so they can be restored
 *    (paused) after a full process kill; on a fresh service start the last queue is reloaded.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var musicRepository: MusicRepository
    @Inject lateinit var queuePersistence: QueuePersistenceRepository

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickerJob: Job? = null
    private var countedCurrentItem = false

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            // Request audio focus + pause when headphones are unplugged.
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply { addListener(playerListener) }

        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer).build()

        restoreQueue()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /** If the user swipes the app away while nothing is playing, tear the service down. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val currentPlayer = player
        if (currentPlayer == null || !currentPlayer.playWhenReady || currentPlayer.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopTicker()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }

    // --- Player events: play-count + persistence triggers ---------------------

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // New track (or a repeat) starts uncounted.
            countedCurrentItem = false
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startTicker() else stopTicker()
        }

        override fun onEvents(player: Player, events: Player.Events) {
            // Save when the queue, current item, modes, or play/pause change.
            if (events.containsAny(
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                    Player.EVENT_REPEAT_MODE_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_POSITION_DISCONTINUITY,
                )
            ) {
                savePlaybackState()
            }
        }
    }

    /** Runs while playing: counts a play after ~5s, and saves position every few seconds. */
    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            var tick = 0
            while (isActive) {
                maybeCountPlay()
                if (tick % POSITION_SAVE_EVERY_TICKS == 0) savePlaybackState()
                tick++
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private suspend fun maybeCountPlay() {
        if (countedCurrentItem) return
        val currentPlayer = player ?: return
        if (currentPlayer.isPlaying && currentPlayer.currentPosition >= PLAY_COUNT_THRESHOLD_MS) {
            val trackId = currentPlayer.currentMediaItem?.mediaId?.toLongOrNull() ?: return
            countedCurrentItem = true // guard against double-counting this play
            musicRepository.incrementPlayCount(trackId)
        }
    }

    // --- Queue persistence ----------------------------------------------------

    /** Snapshot the current queue + playback state and persist it (off the main thread). */
    private fun savePlaybackState() {
        val p = player ?: return
        val count = p.mediaItemCount
        if (count == 0) {
            serviceScope.launch { queuePersistence.clear() }
            return
        }
        val trackIds = (0 until count).map { p.getMediaItemAt(it).mediaId.toLongOrNull() ?: -1L }
        val index = p.currentMediaItemIndex
        val position = p.currentPosition.coerceAtLeast(0L)
        val shuffle = p.shuffleModeEnabled
        val repeat = p.repeatMode
        serviceScope.launch {
            queuePersistence.save(trackIds, index, position, shuffle, repeat)
        }
    }

    /** On a fresh service start with an empty player, reload the last persisted queue (paused). */
    private fun restoreQueue() {
        serviceScope.launch {
            val saved = queuePersistence.load() ?: return@launch
            val p = player ?: return@launch
            if (p.mediaItemCount > 0) return@launch // a live queue already exists — don't clobber

            val library = musicRepository.observeTracks().first().associateBy { it.id }
            val tracks = saved.trackIds.mapNotNull { library[it] }
            if (tracks.isEmpty()) return@launch

            val startIndex = saved.currentIndex.coerceIn(0, tracks.lastIndex)
            p.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, saved.positionMs.coerceAtLeast(0L))
            p.shuffleModeEnabled = saved.shuffleEnabled
            p.repeatMode = saved.repeatMode
            p.playWhenReady = false // restore paused; user taps play to resume
            p.prepare()
        }
    }

    private companion object {
        const val PLAY_COUNT_THRESHOLD_MS = 5_000L
        const val POLL_INTERVAL_MS = 1_000L
        const val POSITION_SAVE_EVERY_TICKS = 5 // ~every 5s while playing
    }
}
