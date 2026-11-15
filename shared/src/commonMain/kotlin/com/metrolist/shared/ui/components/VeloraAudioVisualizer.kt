/*
 * Velora Project - Audio spectrum visualizer
 * Licensed under GPL-3.0 | Derived from the Metrolist project
 */

package com.metrolist.shared.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.metrolist.shared.equalizer.AudioSpectrumMeter
import kotlinx.coroutines.delay

/**
 * Live, lightweight spectrum visualization driven by the shared post-processing PCM meter.
 * The meter only collects samples while it has active consumers.
 */
@Composable
fun VeloraAudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFB69CFF),
    glowColor: Color = Color(0xFF70E1D1),
    barCount: Int = 36,
) {
    var levels by remember { mutableStateOf(FloatArray(AudioSpectrumMeter.bandCount)) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            AudioSpectrumMeter.addConsumer()
            try {
                while (true) {
                    levels = AudioSpectrumMeter.levels()
                    delay(32)
                }
            } finally {
                AudioSpectrumMeter.removeConsumer()
            }
        } else {
            levels = FloatArray(AudioSpectrumMeter.bandCount)
        }
    }

    Canvas(
        modifier = modifier.fillMaxWidth().height(88.dp),
    ) {
        if (barCount <= 0) return@Canvas
        val gap = size.width * 0.012f
        val barWidth = ((size.width - gap * (barCount - 1)) / barCount).coerceAtLeast(1f)
        val centerY = size.height * 0.52f
        val maxHalfHeight = size.height * 0.46f
        val paint = Brush.verticalGradient(
            colors = listOf(glowColor, barColor, barColor.copy(alpha = 0.42f)),
            startY = 0f,
            endY = size.height,
        )

        for (index in 0 until barCount) {
            val sourcePosition = if (barCount == 1) 0f else index.toFloat() / (barCount - 1)
            val bandPosition = sourcePosition * (levels.size - 1)
            val lower = bandPosition.toInt().coerceIn(levels.indices)
            val upper = (lower + 1).coerceIn(levels.indices)
            val fraction = bandPosition - lower
            val magnitude = levels[lower] * (1f - fraction) + levels[upper] * fraction
            val envelope = (0.16f + magnitude * 0.84f) * maxHalfHeight
            val x = index * (barWidth + gap) + barWidth / 2f

            drawLine(
                brush = paint,
                start = Offset(x, centerY - envelope),
                end = Offset(x, centerY + envelope),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
