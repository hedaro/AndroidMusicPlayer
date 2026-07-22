package com.hedaro.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Playlist

/**
 * Dialog for adding the selected track to a playlist. Lists existing playlists plus a
 * "New playlist…" entry that switches to a name prompt. The caller supplies the track context via
 * the callbacks (so this dialog stays track-agnostic).
 */
@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onAddToExisting: (playlistId: Long) -> Unit,
    onCreateAndAdd: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var creating by remember { mutableStateOf(false) }

    if (creating) {
        PlaylistNameDialog(
            title = stringResource(R.string.dialog_new_playlist_title),
            confirmLabel = stringResource(R.string.action_create),
            onConfirm = onCreateAndAdd,
            onDismiss = onDismiss,
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.action_add_to_playlist)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    item {
                        ListItem(
                            modifier = Modifier.clickable { creating = true },
                            leadingContent = { Icon(Icons.Filled.Add, contentDescription = null) },
                            headlineContent = { Text(stringResource(R.string.action_new_playlist)) },
                        )
                    }
                    items(playlists, key = { it.id }) { playlist ->
                        ListItem(
                            modifier = Modifier.clickable { onAddToExisting(playlist.id) },
                            headlineContent = { Text(playlist.name) },
                            supportingContent = {
                                Text(stringResource(R.string.playlist_track_count, playlist.trackCount))
                            },
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}
