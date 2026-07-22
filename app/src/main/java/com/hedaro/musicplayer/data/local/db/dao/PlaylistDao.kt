package com.hedaro.musicplayer.data.local.db.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hedaro.musicplayer.data.local.db.entity.PlaylistEntity
import com.hedaro.musicplayer.data.local.db.entity.PlaylistTrackCrossRef
import kotlinx.coroutines.flow.Flow

/** A playlist row plus its computed track count, for list display. */
data class PlaylistWithCount(
    @Embedded val playlist: PlaylistEntity,
    val trackCount: Int,
)

/** CRUD for playlists and their ordered track membership. */
@Dao
interface PlaylistDao {

    // --- Playlists ---------------------------------------------------------

    @Query(
        """
        SELECT p.*, (
            SELECT COUNT(*) FROM playlist_track_cross_ref x WHERE x.playlistId = p.id
        ) AS trackCount
        FROM playlists p
        ORDER BY p.name COLLATE NOCASE ASC
        """,
    )
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: Long): PlaylistEntity?

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    // --- Membership --------------------------------------------------------

    /** Ordered cross-refs for a playlist (position ascending). */
    @Query("SELECT * FROM playlist_track_cross_ref WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observeTrackRefs(playlistId: Long): Flow<List<PlaylistTrackCrossRef>>

    @Query("SELECT * FROM playlist_track_cross_ref WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getTrackRefs(playlistId: Long): List<PlaylistTrackCrossRef>

    /** Largest current position in a playlist, or -1 if empty (so the next append is position 0). */
    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackRef(ref: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deleteTrackRef(playlistId: Long, trackId: Long)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    /** Rewrites all cross-refs for a playlist in a single transaction (used for reordering). */
    @Transaction
    suspend fun replaceTrackRefs(playlistId: Long, refs: List<PlaylistTrackCrossRef>) {
        clearPlaylist(playlistId)
        refs.forEach { insertTrackRef(it) }
    }
}
