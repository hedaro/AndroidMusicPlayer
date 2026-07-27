package com.hedaro.musicplayer.ui.queue

import androidx.lifecycle.ViewModel
import com.hedaro.musicplayer.playback.PlaybackConnection
import com.hedaro.musicplayer.playback.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Backs the Queue screen: reads the live queue and forwards jump/reorder/remove to playback. */
@HiltViewModel
class QueueViewModel @Inject constructor(
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackConnection.playbackState

    fun jumpTo(index: Int) = playbackConnection.jumpTo(index)
    fun move(from: Int, to: Int) = playbackConnection.moveInQueue(from, to)
    fun remove(index: Int) = playbackConnection.removeFromQueue(index)
    fun clearQueue() = playbackConnection.clearQueue()
}
