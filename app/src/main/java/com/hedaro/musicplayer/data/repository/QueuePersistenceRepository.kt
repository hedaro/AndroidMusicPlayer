package com.hedaro.musicplayer.data.repository

import com.hedaro.musicplayer.data.local.db.dao.QueueDao
import com.hedaro.musicplayer.data.local.db.entity.PlaybackStateEntity
import com.hedaro.musicplayer.data.local.db.entity.QueueItemEntity
import javax.inject.Inject
import javax.inject.Singleton

/** A restored playback snapshot (queue track ids + where/how it was playing). */
data class SavedPlayback(
    val trackIds: List<Long>,
    val currentIndex: Int,
    val positionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: Int,
)

/** Saves/loads the play queue + playback state so it survives a full process kill. */
@Singleton
class QueuePersistenceRepository @Inject constructor(
    private val queueDao: QueueDao,
) {
    suspend fun save(
        trackIds: List<Long>,
        currentIndex: Int,
        positionMs: Long,
        shuffleEnabled: Boolean,
        repeatMode: Int,
    ) {
        val items = trackIds.mapIndexed { index, id -> QueueItemEntity(position = index, trackId = id) }
        queueDao.save(
            items = items,
            state = PlaybackStateEntity(
                currentIndex = currentIndex,
                positionMs = positionMs,
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
            ),
        )
    }

    suspend fun load(): SavedPlayback? {
        val queue = queueDao.getQueue()
        if (queue.isEmpty()) return null
        val state = queueDao.getState() ?: return null
        return SavedPlayback(
            trackIds = queue.map { it.trackId },
            currentIndex = state.currentIndex,
            positionMs = state.positionMs,
            shuffleEnabled = state.shuffleEnabled,
            repeatMode = state.repeatMode,
        )
    }

    suspend fun clear() = queueDao.clearAll()
}
