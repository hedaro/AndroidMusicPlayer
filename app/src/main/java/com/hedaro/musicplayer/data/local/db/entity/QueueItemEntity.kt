package com.hedaro.musicplayer.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One persisted queue slot: a track id at an ordered position. */
@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey val position: Int,
    val trackId: Long,
)
