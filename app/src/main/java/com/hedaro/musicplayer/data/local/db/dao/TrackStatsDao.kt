package com.hedaro.musicplayer.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.hedaro.musicplayer.data.local.db.entity.TrackStatsEntity
import kotlinx.coroutines.flow.Flow

/** Reads/writes per-track favorite + play-count stats. */
@Dao
interface TrackStatsDao {

    /** All stats rows, for merging onto the library. */
    @Query("SELECT * FROM track_stats")
    fun observeAll(): Flow<List<TrackStatsEntity>>

    @Query("SELECT * FROM track_stats WHERE trackId = :trackId")
    fun observe(trackId: Long): Flow<TrackStatsEntity?>

    /**
     * Set (or clear) the favorite flag, creating the row if needed. Upsert via
     * INSERT ... ON CONFLICT so a first-time favorite doesn't require a prior row.
     */
    @Query(
        """
        INSERT INTO track_stats (trackId, isFavorite, playCount) VALUES (:trackId, :favorite, 0)
        ON CONFLICT(trackId) DO UPDATE SET isFavorite = :favorite
        """,
    )
    suspend fun setFavorite(trackId: Long, favorite: Boolean)

    /**
     * Increment the play count by one, creating the row (count = 1) if it doesn't exist yet.
     * Called from the playback layer after ~5s of playback.
     */
    @Query(
        """
        INSERT INTO track_stats (trackId, isFavorite, playCount) VALUES (:trackId, 0, 1)
        ON CONFLICT(trackId) DO UPDATE SET playCount = playCount + 1
        """,
    )
    suspend fun incrementPlayCount(trackId: Long)
}
