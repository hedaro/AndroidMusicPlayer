package com.hedaro.musicplayer.data.model

import android.net.Uri

/**
 * A playable audio track from the device's local library.
 *
 * The bulk of the fields come from `MediaStore` (read-only system index of audio files).
 * [isFavorite] and [playCount] are app-owned user data merged in from Room by the repository —
 * MediaStore cannot store them (see `TrackStatsEntity`).
 *
 * @property id stable `MediaStore._ID` — also the key used for favorites / play count.
 * @property uri content URI used to actually play the track.
 * @property albumArtUri content URI for the album artwork (loaded by Coil).
 * @property durationMs track length in milliseconds.
 */
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: Uri,
    val albumArtUri: Uri,
    val trackNumber: Int? = null,
    val dateAddedSec: Long = 0L,
    /** Full parent-directory path of the audio file, used for folder browsing. */
    val folder: String = "",
    // User data merged from Room:
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
)
