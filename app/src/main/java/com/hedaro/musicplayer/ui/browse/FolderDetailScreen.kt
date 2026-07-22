package com.hedaro.musicplayer.ui.browse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedaro.musicplayer.ui.components.CollectionDetailScreen

@Composable
fun FolderDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderDetailViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()

    CollectionDetailScreen(
        title = viewModel.folderName,
        tracks = tracks,
        onBack = onBack,
        onPlay = viewModel::play,
        onShufflePlay = viewModel::shufflePlay,
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = modifier,
    )
}
