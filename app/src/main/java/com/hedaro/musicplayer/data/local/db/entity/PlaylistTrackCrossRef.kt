package com.hedaro.musicplayer.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * Junction row linking a playlist to a track (by `MediaStore` id), with an explicit
 * [position] so playlist ordering is preserved and reorderable.
 *
 * Tracks themselves are NOT stored in Room (they live in MediaStore) — only their ids
 * are referenced here, so there is no foreign key to a tracks table.
 */
@Entity(
    tableName = "playlist_track_cross_ref",
    primaryKeys = ["playlistId", "trackId"],
    indices = [Index("playlistId"), Index("trackId")],
)
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: Long,
    val position: Int,
)
