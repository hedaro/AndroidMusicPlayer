package com.hedaro.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album as AlbumIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Album

/** A row in the Albums list: album art, title, and "artist · N tracks". */
@Composable
fun AlbumRow(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fallback = rememberVectorPainter(Icons.Filled.AlbumIcon)
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            AsyncImage(
                model = album.albumArtUri,
                contentDescription = stringResource(R.string.cd_album_art),
                contentScale = ContentScale.Crop,
                placeholder = fallback,
                error = fallback,
                fallback = fallback,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
        },
        headlineContent = {
            Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(
                text = "${album.artist}  •  ${stringResource(R.string.playlist_track_count, album.trackCount)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}
