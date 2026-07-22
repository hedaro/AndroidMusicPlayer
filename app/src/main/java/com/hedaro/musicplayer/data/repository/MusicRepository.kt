package com.hedaro.musicplayer.data.repository

import com.hedaro.musicplayer.data.local.MediaStoreDataSource
import com.hedaro.musicplayer.data.local.db.dao.TrackStatsDao
import com.hedaro.musicplayer.data.local.db.entity.TrackStatsEntity
import com.hedaro.musicplayer.data.model.Album
import com.hedaro.musicplayer.data.model.Folder
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.model.TrackSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the music library.
 *
 * Combines the read-only [MediaStoreDataSource] library with the app-owned [TrackStatsDao]
 * (favorite + play count), so every [Track] emitted already carries its user data. Because both
 * inputs are [Flow]s, toggling a favorite or bumping a play count re-emits the merged list and the
 * UI updates automatically.
 */
@Singleton
class MusicRepository @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val trackStatsDao: TrackStatsDao,
) {
    /** All library tracks, with favorite/play-count merged in, ordered by [sort]. */
    fun observeTracks(sort: TrackSort = TrackSort.TITLE): Flow<List<Track>> =
        combine(
            mediaStoreDataSource.observeTracks(),
            trackStatsDao.observeAll(),
        ) { tracks, stats ->
            val statsById = stats.associateBy { it.trackId }
            tracks
                .map { track ->
                    val s = statsById[track.id] ?: return@map track
                    track.copy(isFavorite = s.isFavorite, playCount = s.playCount)
                }
                .sortedWith(comparatorFor(sort))
        }

    /** Just the favorited tracks, ordered by [sort]. */
    fun observeFavorites(sort: TrackSort = TrackSort.TITLE): Flow<List<Track>> =
        observeTracks(sort).map { list -> list.filter(Track::isFavorite) }

    // --- Browsing: albums & folders (derived from the library) --------------

    /** The library grouped into albums, sorted by album title. */
    fun observeAlbums(): Flow<List<Album>> =
        observeTracks().map { tracks ->
            tracks.groupBy { it.albumId }
                .map { (albumId, albumTracks) ->
                    val first = albumTracks.first()
                    Album(
                        id = albumId,
                        title = first.album,
                        artist = first.artist,
                        albumArtUri = first.albumArtUri,
                        trackCount = albumTracks.size,
                    )
                }
                .sortedBy { it.title.lowercase() }
        }

    /** Tracks of one album, ordered by track number then title. */
    fun observeAlbumTracks(albumId: Long): Flow<List<Track>> =
        observeTracks().map { tracks ->
            tracks.filter { it.albumId == albumId }
                .sortedWith(compareBy({ it.trackNumber ?: Int.MAX_VALUE }, { it.title.lowercase() }))
        }

    /** The library grouped into device folders, sorted by folder name. */
    fun observeFolders(): Flow<List<Folder>> =
        observeTracks().map { tracks ->
            tracks.groupBy { it.folder }
                .map { (path, folderTracks) ->
                    Folder(
                        path = path,
                        name = path.substringAfterLast('/').ifBlank { path },
                        trackCount = folderTracks.size,
                    )
                }
                .sortedBy { it.name.lowercase() }
        }

    /** Tracks in one folder, ordered by title. */
    fun observeFolderTracks(folderPath: String): Flow<List<Track>> =
        observeTracks().map { tracks -> tracks.filter { it.folder == folderPath } }

    /** Observe the stored stats (favorite + play count) for a single track; null if none yet. */
    fun observeStats(trackId: Long): Flow<TrackStatsEntity?> = trackStatsDao.observe(trackId)

    /** Turn a track's favorite flag on/off. */
    suspend fun setFavorite(trackId: Long, favorite: Boolean) =
        trackStatsDao.setFavorite(trackId, favorite)

    /** Increment a track's play count (called by the playback layer after ~5s of playback). */
    suspend fun incrementPlayCount(trackId: Long) =
        trackStatsDao.incrementPlayCount(trackId)

    private fun comparatorFor(sort: TrackSort): Comparator<Track> = when (sort) {
        TrackSort.TITLE ->
            compareBy(String.CASE_INSENSITIVE_ORDER, Track::title)

        TrackSort.ARTIST ->
            compareBy(String.CASE_INSENSITIVE_ORDER, Track::artist)
                .thenBy(String.CASE_INSENSITIVE_ORDER, Track::title)

        TrackSort.RECENTLY_ADDED ->
            compareByDescending<Track> { it.dateAddedSec }
                .thenBy(String.CASE_INSENSITIVE_ORDER, Track::title)

        TrackSort.MOST_PLAYED ->
            compareByDescending<Track> { it.playCount }
                .thenBy(String.CASE_INSENSITIVE_ORDER, Track::title)
    }
}
