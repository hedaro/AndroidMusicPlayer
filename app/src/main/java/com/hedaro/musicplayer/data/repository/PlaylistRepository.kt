package com.hedaro.musicplayer.data.repository

import com.hedaro.musicplayer.data.local.db.dao.PlaylistDao
import com.hedaro.musicplayer.data.local.db.entity.PlaylistEntity
import com.hedaro.musicplayer.data.local.db.entity.PlaylistTrackCrossRef
import com.hedaro.musicplayer.data.model.Playlist
import com.hedaro.musicplayer.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user playlists (Room), resolving their track membership against the live library
 * via [MusicRepository]. Playlists store only track ids + positions; the actual [Track] data
 * is looked up from the current library so playlists stay in sync as files change.
 */
@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val musicRepository: MusicRepository,
) {
    /** All playlists with their track counts, for list display. */
    fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observePlaylists().map { rows ->
            rows.map { row ->
                Playlist(id = row.playlist.id, name = row.playlist.name, trackCount = row.trackCount)
            }
        }

    /**
     * The tracks of a playlist, in saved order, resolved against the current library.
     * Entries whose underlying file no longer exists are silently dropped.
     */
    fun observePlaylistTracks(playlistId: Long): Flow<List<Track>> =
        combine(
            playlistDao.observeTrackRefs(playlistId),
            musicRepository.observeTracks(),
        ) { refs, tracks ->
            val byId = tracks.associateBy { it.id }
            refs.mapNotNull { byId[it.trackId] } // refs already ordered by position
        }

    suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name))

    suspend fun renamePlaylist(playlistId: Long, name: String) {
        val existing = playlistDao.getPlaylist(playlistId) ?: return
        playlistDao.updatePlaylist(existing.copy(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) = playlistDao.deletePlaylist(playlistId)

    /** Append a track to the end of a playlist (duplicates are ignored). */
    suspend fun addTrack(playlistId: Long, trackId: Long) {
        val nextPosition = playlistDao.maxPosition(playlistId) + 1
        playlistDao.insertTrackRef(PlaylistTrackCrossRef(playlistId, trackId, nextPosition))
    }

    suspend fun removeTrack(playlistId: Long, trackId: Long) =
        playlistDao.deleteTrackRef(playlistId, trackId)

    /** Persist a new ordering; [orderedTrackIds] is the full track list in the desired order. */
    suspend fun reorder(playlistId: Long, orderedTrackIds: List<Long>) {
        val refs = orderedTrackIds.mapIndexed { index, trackId ->
            PlaylistTrackCrossRef(playlistId, trackId, index)
        }
        playlistDao.replaceTrackRefs(playlistId, refs)
    }
}
