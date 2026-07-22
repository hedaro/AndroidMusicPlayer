package com.hedaro.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Track

/**
 * A single track row: album art, title/artist·duration, and a favorite toggle.
 * [isCurrent] highlights the row that's currently playing.
 */
@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
) {
    val fallback = rememberVectorPainter(Icons.Filled.MusicNote)
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            AsyncImage(
                model = track.albumArtUri,
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
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Unspecified,
            )
        },
        supportingContent = {
            Text(
                text = "${track.artist}  •  ${com.hedaro.musicplayer.util.formatDuration(track.durationMs)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(R.string.cd_toggle_favorite),
                    tint = if (track.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                )
            }
        },
    )
}
