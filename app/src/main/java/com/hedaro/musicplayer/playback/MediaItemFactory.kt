package com.hedaro.musicplayer.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.hedaro.musicplayer.data.model.Track

/**
 * Builds a Media3 [MediaItem] from a [Track]. The `mediaId` is the track id, which lets the
 * service map playback (and restored queues) back to a track. Shared by [PlaybackConnection]
 * (building queues from the UI) and [PlaybackService] (restoring a persisted queue).
 */
internal fun Track.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri)
                .build(),
        )
        .build()
