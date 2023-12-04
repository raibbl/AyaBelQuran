package com.raibbl.ayabelquran.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon


@Composable
fun AnimatedScrollHint(position: String) {
//    val infiniteTransition = rememberInfiniteTransition()
//    val offsetY by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 5f, // Adjust the distance
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = 1000, easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ), label = "animation offset"
//    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = if (position == "right") Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Default.Height,
            contentDescription = "scroll",
            modifier = Modifier
//                .offset(y = offsetY.dp)
                .padding(end = 12.dp)
                .size(25.dp)
        )
    }
}
