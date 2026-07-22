package com.hedaro.musicplayer.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A user-created playlist. Its tracks are stored via [PlaylistTrackCrossRef]. */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)
