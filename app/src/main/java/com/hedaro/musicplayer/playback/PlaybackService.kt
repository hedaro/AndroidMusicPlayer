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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background playback service built on Media3.
 *
 * Owns the [ExoPlayer] and a [MediaSession]; the UI drives playback through a `MediaController`
 * (see [PlaybackConnection]). Because playback lives here (not in a ViewModel), it survives the UI
 * being backgrounded and Media3 provides the media notification + lock-screen controls automatically.
 *
 * Also hosts the play-count rule: a track counts as "played" once it has passed
 * [PLAY_COUNT_THRESHOLD_MS] of playback, at most once per play (guarded by [countedCurrentItem]).
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var musicRepository: MusicRepository

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var playCountJob: Job? = null
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
        stopPlayCountTracking()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }

    // --- Play-count rule: count after ~5s of playback, once per play ---------

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // New track (or a repeat) starts uncounted.
            countedCurrentItem = false
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startPlayCountTracking() else stopPlayCountTracking()
        }
    }

    private fun startPlayCountTracking() {
        playCountJob?.cancel()
        playCountJob = serviceScope.launch {
            while (isActive) {
                maybeCountPlay()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopPlayCountTracking() {
        playCountJob?.cancel()
        playCountJob = null
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

    private companion object {
        const val PLAY_COUNT_THRESHOLD_MS = 5_000L
        const val POLL_INTERVAL_MS = 1_000L
    }
}
