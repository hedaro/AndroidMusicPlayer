package com.hedaro.musicplayer.data.model

/**
 * A user-created playlist. Persisted in Room (see `PlaylistEntity`); its tracks are
 * referenced by `MediaStore` id and loaded from the library on demand.
 *
 * @property trackCount number of tracks in the playlist (computed, for list display).
 */
data class Playlist(
    val id: Long,
    val name: String,
    val trackCount: Int = 0,
)
