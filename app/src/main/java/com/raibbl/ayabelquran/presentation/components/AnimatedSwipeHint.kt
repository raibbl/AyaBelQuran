package com.raibbl.ayabelquran.presentation.components
import androidx.compose.animation.core.Animatable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon


@Composable
fun AnimatedSwipeHint(
    direction: String,
    modifier: Modifier = Modifier,
    animateOnEntry: Boolean = true
) {
    val offset = remember { Animatable(0f) }

    LaunchedEffect(direction, animateOnEntry) {
        offset.snapTo(0f)
        if (animateOnEntry) {
            repeat(3) {
                offset.animateTo(
                    targetValue = 5f,
                    animationSpec = tween(durationMillis = 280, easing = LinearEasing)
                )
                offset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 280, easing = LinearEasing)
                )
            }
        }
    }

    val directionalOffset = if (direction == "right") offset.value else -offset.value
    val edgePadding = if (direction == "right") Modifier.padding(end = 16.dp) else Modifier.padding(start = 16.dp)

    Icon(
        imageVector = if (direction == "right") Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
        contentDescription = "Swipe",
        modifier = modifier
            .then(edgePadding)
            .offset(x = directionalOffset.dp)
            .size(25.dp)
    )
}
