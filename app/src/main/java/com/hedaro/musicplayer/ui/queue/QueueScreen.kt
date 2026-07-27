package com.hedaro.musicplayer.ui.queue

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.playback.QueueItem
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel(),
) {
    val state by viewModel.playbackState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_queue)) },
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
        if (state.queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.empty_queue))
            }
        } else {
            QueueList(
                queue = state.queue,
                currentIndex = state.currentIndex,
                innerPadding = innerPadding,
                onJump = viewModel::jumpTo,
                onMove = viewModel::move,
                onRemove = viewModel::remove,
            )
        }
    }
}

@Composable
private fun QueueList(
    queue: List<QueueItem>,
    currentIndex: Int,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onJump: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    // Local copy so drags animate smoothly; re-syncs whenever the live queue changes.
    var items by remember { mutableStateOf(queue) }
    LaunchedEffect(queue) { items = queue }

    // Scroll the current track into view when the screen first opens.
    LaunchedEffect(Unit) {
        if (currentIndex in items.indices) lazyListState.scrollToItem(currentIndex)
    }

    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        items = items.toMutableList().apply { add(to.index, removeAt(from.index)) }
        onMove(from.index, to.index) // apply to the live queue
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding,
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            ReorderableItem(reorderState, key = item.id) { isDragging ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) {
                            onRemove(index)
                            true
                        } else {
                            false
                        }
                    },
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = { QueueDismissBackground() },
                ) {
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) 6.dp else 0.dp,
                        label = "queueDragElevation",
                    )
                    Surface(shadowElevation = elevation) {
                        QueueRow(
                            item = item,
                            isCurrent = index == currentIndex,
                            onClick = { onJump(index) },
                            modifier = Modifier.longPressDraggableHandle(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    item: QueueItem,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fallback = rememberVectorPainter(Icons.Filled.MusicNote)
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            AsyncImage(
                model = item.artworkUri,
                contentDescription = null,
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
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Unspecified,
            )
        },
        supportingContent = {
            Text(item.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            if (isCurrent) {
                Icon(
                    Icons.Filled.GraphicEq,
                    contentDescription = stringResource(R.string.cd_now_playing_indicator),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Composable
private fun QueueDismissBackground() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = MaterialTheme.colorScheme.onErrorContainer
        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_remove_from_queue), tint = tint)
        Icon(Icons.Filled.Delete, contentDescription = null, tint = tint)
    }
}
