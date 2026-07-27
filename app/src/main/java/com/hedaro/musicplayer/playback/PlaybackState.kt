package com.hedaro.musicplayer.playback

import android.net.Uri

/** Loop mode, mapped from Media3's repeat-mode ints into a UI-friendly enum. */
enum class RepeatMode { OFF, ONE, ALL }

/** A single entry in the play queue (a snapshot of a Media3 timeline item). */
data class QueueItem(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUri: Uri? = null,
)

/**
 * Immutable snapshot of the current playback state, exposed to the UI as a StateFlow by
 * [PlaybackConnection]. Keeps the UI fully decoupled from Media3 / ExoPlayer types.
 */
data class PlaybackState(
    val isConnected: Boolean = false,
    val currentTrackId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUri: Uri? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val queueSize: Int = 0,
    /** The full play queue, in timeline order. */
    val queue: List<QueueItem> = emptyList(),
    /** Index of the currently-playing item within [queue], or -1 if none. */
    val currentIndex: Int = -1,
)
