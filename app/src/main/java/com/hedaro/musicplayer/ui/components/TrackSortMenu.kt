package com.hedaro.musicplayer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.TrackSort

/** Sort icon + dropdown for track lists (Library, Favorites). */
@Composable
fun TrackSortMenu(
    currentSort: TrackSort,
    onSortSelected: (TrackSort) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    IconButton(onClick = { menuOpen = true }) {
        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.cd_sort))
    }
    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        TrackSort.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(sortLabel(option)) },
                onClick = {
                    onSortSelected(option)
                    menuOpen = false
                },
                trailingIcon = {
                    if (option == currentSort) Icon(Icons.Filled.Check, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun sortLabel(sort: TrackSort): String = stringResource(
    when (sort) {
        TrackSort.TITLE -> R.string.sort_title
        TrackSort.ARTIST -> R.string.sort_artist
        TrackSort.RECENTLY_ADDED -> R.string.sort_recently_added
        TrackSort.MOST_PLAYED -> R.string.sort_most_played
    },
)
