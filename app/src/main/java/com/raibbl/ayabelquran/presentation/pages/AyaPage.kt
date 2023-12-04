package com.raibbl.ayabelquran.presentation.pages

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.raibbl.ayabelquran.presentation.components.AnimatedScrollHint
import com.raibbl.ayabelquran.presentation.navigation.Screen
import com.raibbl.ayabelquran.presentation.components.AnimatedSwipeHint
import kotlinx.coroutines.launch
import utilities.MediaPlayer

@OptIn(ExperimentalWearMaterialApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun AyaPage(
    ayaText: String,
    onRefresh: () -> Unit,
    navController: NavHostController
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val horizontalScrollState = rememberScrollState()
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { -400.dp.toPx() } to 1
    )
    val focusRequester = rememberActiveFocusRequester()
    val coroutineScope = rememberCoroutineScope()
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
        AnimatedScrollHint(position = "left")
        if (swipeableState.currentValue == 1) {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.tafsirPage.route)
            }
        }

        Column(
            modifier = Modifier
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        horizontalScrollState.scrollBy(it.verticalScrollPixels)

                        horizontalScrollState.animateScrollBy(0f)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable()
                .align(Alignment.TopCenter)
                .verticalScroll(horizontalScrollState)
        ) {

            Text(
                text = ayaText,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 35.dp, bottom = 10.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.primary
            )

            Button(
                onClick = {
                    navController.navigate(Screen.surahListGuessScreen.route)
                },
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 5.dp, bottom = 10.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "خمن السورة",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
            // Navigation Bar at the bottom
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .padding(bottom = 20.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {


                Button(onClick = { MediaPlayer.playAudioFromUrl() }) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(25.dp),

                        )
                }
                Spacer(modifier =  Modifier.width(5.dp) )
                Button(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(25.dp),
//                tint = Color.White // Change icon color if needed
                    )
                }
            }

        }


    }

}
