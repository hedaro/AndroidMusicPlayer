package com.hedaro.musicplayer.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.playback.RepeatMode
import com.hedaro.musicplayer.ui.components.StepControls
import com.hedaro.musicplayer.ui.components.TrackSeekBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val state by viewModel.playbackState.collectAsStateWithLifecycle()
    val extras by viewModel.extras.collectAsStateWithLifecycle()
    val fallback = rememberVectorPainter(Icons.Filled.MusicNote)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_now_playing)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            imageVector = if (extras.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.cd_toggle_favorite),
                            tint = if (extras.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            AsyncImage(
                model = state.artworkUri,
                contentDescription = stringResource(R.string.cd_album_art),
                contentScale = ContentScale.Crop,
                placeholder = fallback,
                error = fallback,
                fallback = fallback,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
            )

            Spacer(Modifier.height(28.dp))
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (extras.playCount > 0) {
                Text(
                    text = stringResource(R.string.plays_count, extras.playCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(20.dp))
            TrackSeekBar(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                onSeek = viewModel::seekTo,
            )

            Spacer(Modifier.height(4.dp))
            StepControls(onStep = viewModel::stepBy)

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = viewModel::toggleShuffle) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = stringResource(R.string.cd_shuffle),
                        tint = if (state.shuffleEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                IconButton(onClick = viewModel::previous, enabled = state.hasPrevious) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.cd_previous))
                }
                FilledIconButton(
                    onClick = viewModel::playPause,
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(
                            if (state.isPlaying) R.string.cd_pause else R.string.cd_play,
                        ),
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(onClick = viewModel::next, enabled = state.hasNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.cd_next))
                }
                IconButton(onClick = viewModel::cycleRepeat) {
                    val onColor = MaterialTheme.colorScheme.primary
                    val offColor = MaterialTheme.colorScheme.onSurfaceVariant
                    when (state.repeatMode) {
                        RepeatMode.OFF -> Icon(Icons.Filled.Repeat, stringResource(R.string.cd_repeat), tint = offColor)
                        RepeatMode.ALL -> Icon(Icons.Filled.Repeat, stringResource(R.string.cd_repeat), tint = onColor)
                        RepeatMode.ONE -> Icon(Icons.Filled.RepeatOne, stringResource(R.string.cd_repeat), tint = onColor)
                    }
                }
            }
        }
    }
}
