package com.hedaro.musicplayer.data.model

/**
 * A device folder containing audio, derived from track file paths.
 *
 * @property path full directory path (the grouping key).
 * @property name last path segment, for display.
 * @property trackCount number of tracks in the folder.
 */
data class Folder(
    val path: String,
    val name: String,
    val trackCount: Int,
)
