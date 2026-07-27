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
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    CollectionDetailScreen(
        title = viewModel.folderName,
        tracks = tracks,
        playlists = playlists,
        onBack = onBack,
        onPlay = viewModel::play,
        onShufflePlay = viewModel::shufflePlay,
        onToggleFavorite = viewModel::toggleFavorite,
        onPlayNext = viewModel::playNext,
        onAddToQueue = viewModel::addToQueue,
        onAddToPlaylist = { playlistId, track -> viewModel.addToPlaylist(playlistId, track.id) },
        onCreatePlaylistWithTrack = { name, track -> viewModel.createPlaylistWithTrack(name, track.id) },
        modifier = modifier,
    )
}
