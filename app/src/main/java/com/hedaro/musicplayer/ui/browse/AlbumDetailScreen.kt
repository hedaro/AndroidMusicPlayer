package com.hedaro.musicplayer.ui.browse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedaro.musicplayer.ui.components.CollectionDetailScreen

@Composable
fun AlbumDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()

    CollectionDetailScreen(
        title = title,
        tracks = tracks,
        onBack = onBack,
        onPlay = viewModel::play,
        onShufflePlay = viewModel::shufflePlay,
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = modifier,
    )
}
