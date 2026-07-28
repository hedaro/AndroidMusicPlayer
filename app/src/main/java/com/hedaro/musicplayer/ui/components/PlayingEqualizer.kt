package com.hedaro.musicplayer.ui.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

// Bar heights (as a fraction of full height) shown when the bars aren't animating.
private val RestingHeights = listOf(0.5f, 0.85f, 0.3f, 0.65f)
// Staggered per-bar durations so the bars never move in lockstep.
private val BarDurations = listOf(520, 360, 620, 440)

/**
 * A little animated equalizer (4 bars) used to mark the currently-playing track. Bars oscillate
 * out of sync while [playing]; when paused — or when the system has animations turned off (reduced
 * motion) — they hold a static resting pattern and no animation runs. Size comes from [modifier]
 * (e.g. `size(24.dp)`).
 */
@Composable
fun PlayingEqualizer(
    playing: Boolean,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val animationsEnabled = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }

    val canvasModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    if (playing && animationsEnabled) {
        val transition = rememberInfiniteTransition(label = "equalizer")
        // Built in a plain loop (fixed 4 iterations) — composable calls can't go in a non-inline
        // lambda like mapIndexed.
        val bars = ArrayList<State<Float>>(BarDurations.size)
        for (i in BarDurations.indices) {
            bars += transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = BarDurations[i], easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar$i",
            )
        }
        // Reading each bar's value in the draw lambda repaints without recomposing.
        EqualizerBars(canvasModifier, color, bars.size) { i -> bars[i].value }
    } else {
        EqualizerBars(canvasModifier, color, RestingHeights.size) { i -> RestingHeights[i] }
    }
}

@Composable
private fun EqualizerBars(
    modifier: Modifier,
    color: Color,
    barCount: Int,
    fractionAt: (Int) -> Float,
) {
    Canvas(modifier = modifier) {
        val gap = size.width * 0.12f
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        val radius = CornerRadius(barWidth * 0.4f, barWidth * 0.4f)
        for (i in 0 until barCount) {
            val barHeight = size.height * fractionAt(i)
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barWidth + gap), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = radius,
            )
        }
    }
}
