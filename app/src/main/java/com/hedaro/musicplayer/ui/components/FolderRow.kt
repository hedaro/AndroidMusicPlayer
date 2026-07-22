package com.hedaro.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Folder

/** A row in the Folders list: folder icon, name, and track count. */
@Composable
fun FolderRow(
    folder: Folder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            Icon(Icons.Filled.Folder, contentDescription = stringResource(R.string.cd_folder))
        },
        headlineContent = {
            Text(folder.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(stringResource(R.string.playlist_track_count, folder.trackCount))
        },
    )
}
