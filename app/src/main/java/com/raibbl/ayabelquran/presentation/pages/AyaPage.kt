package com.raibbl.ayabelquran.presentation.pages

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.raibbl.ayabelquran.presentation.navigation.Screen
import com.raibbl.ayabelquran.presentation.components.AnimatedSwipeHint
import utilities.MediaPlayer

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun AyaPage(
    ayaText: String,
    ayaAudioUrl: String,
    onRefresh: () -> Unit,
    navController: NavHostController
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { -400.dp.toPx() } to 1
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
    ) {
        AnimatedSwipeHint(direction = "right")
        if (swipeableState.currentValue == 1) {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.tafsirPage.route)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = ayaText,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 35.dp, bottom = 40.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.primary
            )
        }

        // Navigation Bar at the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {


            IconButton(onClick = { MediaPlayer.playAudioFromUrl(ayaAudioUrl) }) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(40.dp),

                    )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(40.dp),
//                tint = Color.White // Change icon color if needed
                )
            }
        }
    }

}
