package com.raibbl.ayabelquran.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon


@Composable
fun AnimatedSwipeHint(direction: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f, // Adjust the distance
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animation offset"
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = if (direction == "right") Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Icon(
            imageVector = if (direction == "right") Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
            contentDescription = "Swipe",
            modifier = Modifier
                .offset(x = offsetX.dp)
                .padding(end = 16.dp)
        )
    }
}
