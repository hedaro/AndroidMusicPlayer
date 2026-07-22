package com.hedaro.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.hedaro.musicplayer.util.formatDuration

/** An entry in a [TrackRow]'s overflow menu (e.g. "Add to playlist", "Remove"). */
data class TrackRowMenuItem(val label: String, val onClick: () -> Unit)

/**
 * A single track row: album art, title/artist·duration, a favorite toggle, and an optional
 * overflow menu ([menuItems]). [isCurrent] highlights the currently-playing row.
 */
@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
    menuItems: List<TrackRowMenuItem> = emptyList(),
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
                text = "${track.artist}  •  ${formatDuration(track.durationMs)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_toggle_favorite),
                        tint = if (track.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    )
                }
                if (menuItems.isNotEmpty()) {
                    TrackOverflowMenu(menuItems)
                }
            }
        },
    )
}

@Composable
private fun TrackOverflowMenu(menuItems: List<TrackRowMenuItem>) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_more))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.label) },
                onClick = {
                    item.onClick()
                    expanded = false
                },
            )
        }
    }
}
