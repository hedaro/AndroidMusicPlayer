package com.hedaro.musicplayer.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hedaro.musicplayer.data.local.db.entity.PlaybackStateEntity
import com.hedaro.musicplayer.data.local.db.entity.QueueItemEntity

/** Persists the play queue + a one-row playback-state snapshot (for restore after process death). */
@Dao
interface QueueDao {

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    suspend fun getQueue(): List<QueueItemEntity>

    @Query("SELECT * FROM playback_state WHERE id = 0")
    suspend fun getState(): PlaybackStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(items: List<QueueItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: PlaybackStateEntity)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Query("DELETE FROM playback_state")
    suspend fun clearState()

    /** Replace the persisted queue + state atomically. */
    @Transaction
    suspend fun save(items: List<QueueItemEntity>, state: PlaybackStateEntity) {
        clearQueue()
        insertQueue(items)
        upsertState(state)
    }

    @Transaction
    suspend fun clearAll() {
        clearQueue()
        clearState()
    }
}
