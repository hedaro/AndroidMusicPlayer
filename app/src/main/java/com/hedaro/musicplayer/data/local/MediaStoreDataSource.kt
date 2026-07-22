package com.hedaro.musicplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.hedaro.musicplayer.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the device's local audio library from [MediaStore] (a read-only system index).
 *
 * Exposes the library as a [Flow] that re-emits whenever the underlying media collection
 * changes (files added/removed), via a [ContentObserver]. Queries run on the IO dispatcher.
 *
 * This source knows nothing about favorites / play counts — those are merged in by
 * `MusicRepository` from Room.
 */
@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** The audio collection URI (volume-aware on API 29+). */
    private val collection: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    /** Emits the full track list now and again whenever the media store changes. */
    fun observeTracks(): Flow<List<Track>> = callbackFlow {
        fun emitLatest() {
            launch(Dispatchers.IO) { trySend(queryTracks()) }
        }

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = emitLatest()
        }
        context.contentResolver.registerContentObserver(collection, true, observer)

        emitLatest() // initial load

        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.flowOn(Dispatchers.IO)

    /** One-shot query of all music tracks. Blocking — call off the main thread. */
    private fun queryTracks(): List<Track> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            @Suppress("DEPRECATION")
            MediaStore.Audio.Media.DATA,
        )
        // Include unclassified audio too: freshly-added files can have IS_MUSIC = NULL until a full
        // media scan runs (e.g. dragging files onto the emulator). `NULL != 0` is not true in SQL,
        // so a plain `!= 0` filter would wrongly hide them. Only exclude explicit non-music.
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.IS_MUSIC} IS NULL"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val tracks = mutableListOf<Track>()
        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackNoCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            @Suppress("DEPRECATION")
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                val rawTrackNo = if (cursor.isNull(trackNoCol)) null else cursor.getInt(trackNoCol)
                val filePath = cursor.getString(dataCol).orEmpty()

                tracks += Track(
                    id = id,
                    title = cursor.getString(titleCol) ?: "",
                    artist = cursor.getString(artistCol) ?: "",
                    album = cursor.getString(albumCol) ?: "",
                    albumId = albumId,
                    durationMs = cursor.getLong(durationCol),
                    uri = ContentUris.withAppendedId(collection, id),
                    albumArtUri = ContentUris.withAppendedId(ALBUM_ART_BASE_URI, albumId),
                    // MediaStore encodes disc*1000 + track; keep the low 3 digits as the track number.
                    trackNumber = rawTrackNo?.let { if (it > 1000) it % 1000 else it },
                    dateAddedSec = cursor.getLong(dateAddedCol),
                    folder = filePath.substringBeforeLast('/', missingDelimiterValue = ""),
                )
            }
        }
        return tracks
    }

    private companion object {
        val ALBUM_ART_BASE_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}
