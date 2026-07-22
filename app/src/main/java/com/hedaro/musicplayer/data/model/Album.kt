package com.hedaro.musicplayer.data.model

import android.net.Uri

/**
 * An album grouping of tracks from the local library (derived from `MediaStore`).
 * Used for album views / grouping; playback still happens at the [Track] level.
 */
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtUri: Uri,
    val trackCount: Int,
)
