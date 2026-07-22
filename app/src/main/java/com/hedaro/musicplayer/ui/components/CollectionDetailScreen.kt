package com.hedaro.musicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Track

/**
 * Shared detail view for a collection of tracks (an album or a folder): a titled list with
 * Play / Shuffle buttons and favorite toggles. No reordering (order is intrinsic to the source).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    title: String,
    tracks: List<Track>,
    onBack: () -> Unit,
    onPlay: (Int) -> Unit,
    onShufflePlay: () -> Unit,
    onToggleFavorite: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = { onPlay(0) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(stringResource(R.string.action_play))
                }
                OutlinedButton(onClick = onShufflePlay, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null)
                    Text(stringResource(R.string.action_shuffle))
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                    TrackRow(
                        track = track,
                        onClick = { onPlay(index) },
                        onToggleFavorite = { onToggleFavorite(track) },
                    )
                }
            }
        }
    }
}
