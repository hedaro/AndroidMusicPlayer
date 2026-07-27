package com.hedaro.musicplayer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hedaro.musicplayer.data.local.db.dao.PlaylistDao
import com.hedaro.musicplayer.data.local.db.dao.QueueDao
import com.hedaro.musicplayer.data.local.db.dao.TrackStatsDao
import com.hedaro.musicplayer.data.local.db.entity.PlaybackStateEntity
import com.hedaro.musicplayer.data.local.db.entity.PlaylistEntity
import com.hedaro.musicplayer.data.local.db.entity.PlaylistTrackCrossRef
import com.hedaro.musicplayer.data.local.db.entity.QueueItemEntity
import com.hedaro.musicplayer.data.local.db.entity.TrackStatsEntity

/**
 * The app's Room database: user-owned data only (playlists, per-track stats, and the persisted
 * play queue). The music library itself is NOT stored here — it comes from MediaStore.
 */
@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        TrackStatsEntity::class,
        QueueItemEntity::class,
        PlaybackStateEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun trackStatsDao(): TrackStatsDao
    abstract fun queueDao(): QueueDao
}

/** v1 → v2: add the queue + playback-state tables (playlists/stats are preserved). */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `queue_items` " +
                "(`position` INTEGER NOT NULL, `trackId` INTEGER NOT NULL, PRIMARY KEY(`position`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `playback_state` " +
                "(`id` INTEGER NOT NULL, `currentIndex` INTEGER NOT NULL, `positionMs` INTEGER NOT NULL, " +
                "`shuffleEnabled` INTEGER NOT NULL, `repeatMode` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
    }
}
