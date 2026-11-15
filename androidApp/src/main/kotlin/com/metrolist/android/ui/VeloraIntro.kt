package com.metrolist.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
internal fun VeloraIntro() {
    var visible by remember { mutableStateOf(true) }
    val transition = rememberInfiniteTransition(label = "veloraIntro")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(850, easing = FastOutSlowInEasing)),
        label = "logoPulse",
    )

    LaunchedEffect(Unit) {
        delay(1_850)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(420)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF30245F), Color(0xFF17132E), Color(0xFF090A16)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.size(132.dp).scale(pulse),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "V",
                        fontSize = 104.sp,
                        lineHeight = 110.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
                Text(
                    text = "VELORA",
                    fontSize = 22.sp,
                    letterSpacing = 7.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = "MUSIC, IN YOUR OWN FLOW.",
                    fontSize = 9.sp,
                    letterSpacing = 2.2.sp,
                    color = Color(0xFFB9AEDF),
                )
            }
        }
    }
}
