package com.hedaro.musicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hedaro.musicplayer.R

/**
 * Touch-friendly step controls: jump backward/forward by 5 or 10 seconds. Calls [onStep] with a
 * signed millisecond delta; the connection clamps it to the track bounds.
 */
@Composable
fun StepControls(
    onStep: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        IconButton(onClick = { onStep(-10_000L) }) {
            Icon(Icons.Filled.Replay10, contentDescription = stringResource(R.string.cd_rewind_10))
        }
        IconButton(onClick = { onStep(-5_000L) }) {
            Icon(Icons.Filled.Replay5, contentDescription = stringResource(R.string.cd_rewind_5))
        }
        IconButton(onClick = { onStep(5_000L) }) {
            Icon(Icons.Filled.Forward5, contentDescription = stringResource(R.string.cd_forward_5))
        }
        IconButton(onClick = { onStep(10_000L) }) {
            Icon(Icons.Filled.Forward10, contentDescription = stringResource(R.string.cd_forward_10))
        }
    }
}
