package com.hedaro.musicplayer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hedaro.musicplayer.data.local.db.dao.PlaylistDao
import com.hedaro.musicplayer.data.local.db.dao.TrackStatsDao
import com.hedaro.musicplayer.data.local.db.entity.PlaylistEntity
import com.hedaro.musicplayer.data.local.db.entity.PlaylistTrackCrossRef
import com.hedaro.musicplayer.data.local.db.entity.TrackStatsEntity

/**
 * The app's Room database: user-owned data only (playlists + per-track stats).
 * The music library itself is NOT stored here — it comes from MediaStore.
 */
@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        TrackStatsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun trackStatsDao(): TrackStatsDao
}
