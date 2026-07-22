package com.hedaro.musicplayer.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * App-owned per-track user data that MediaStore can't hold: the favorite flag and play count.
 * Keyed by the stable `MediaStore._ID`. A row exists only once a track has been favorited or played;
 * absence means "not favorite, 0 plays" (the repository fills defaults).
 */
@Entity(tableName = "track_stats")
data class TrackStatsEntity(
    @PrimaryKey val trackId: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
)
