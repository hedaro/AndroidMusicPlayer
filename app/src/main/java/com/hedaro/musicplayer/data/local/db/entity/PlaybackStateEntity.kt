package com.hedaro.musicplayer.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row (id = 0) snapshot of playback state, saved alongside the persisted queue. */
@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey val id: Int = 0,
    val currentIndex: Int,
    val positionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: Int,
)
