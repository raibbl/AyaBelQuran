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
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.raibbl.ayabelquran.presentation.components.AnimatedSwipeHint
import com.raibbl.ayabelquran.presentation.navigation.Screen
import kotlinx.coroutines.launch



@OptIn(
    ExperimentalWearMaterialApi::class, ExperimentalWearFoundationApi::class,
    ExperimentalHorologistApi::class
)
@Composable
fun AyaPage(
    ayaText: String,
    onRefresh: () -> Unit,
    navController: NavHostController
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { -400.dp.toPx() } to 1,
        with(LocalDensity.current) { 400.dp.toPx() } to -1
    )
    val focusRequester = rememberActiveFocusRequester()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.SingleButton,
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(

            columnState = columnState

        ) {
            item {

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


                    if (swipeableState.currentValue == 1) {
                        LaunchedEffect(Unit) {
                            navController.navigate(Screen.tafsirPage.route)
                        }
                    }

                    if (swipeableState.currentValue == -1) {
                        LaunchedEffect(Unit) {
                            navController.navigate(Screen.surahAudioPageScreen.route)
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.End // Align everything to the right
                    ) {
                        Spacer(modifier = Modifier.height(50.dp)) // Push the hint to a fixed vertical position


                            AnimatedSwipeHint(direction = "right")
                        AnimatedSwipeHint(direction = "left")

                    }

                    Column(
                        modifier = Modifier
                            .onRotaryScrollEvent {
                                coroutineScope.launch {
                                    columnState.scrollBy(it.verticalScrollPixels)

                                    columnState.animateScrollBy(0f)
                                }
                                true
                            }
                            .focusRequester(focusRequester) // Apply the focus requester correctly
                            .focusable() // Ensure the composable is focusable
                            .align(Alignment.TopCenter)
                    ) {

                        Text(
                            text = ayaText,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .padding(start = 15.dp, end = 15.dp, bottom = 10.dp)
                                .align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colors.primary
                        )
                        Button(
                            onClick = {
                                navController.navigate(Screen.surahListGuessScreen.route)
                            },
                            modifier = Modifier
                                .padding(top = 5.dp, bottom = 10.dp)
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


                            Button(
                                onClick = {
                                    MediaPlayer.playAudioFromUrl(context)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play",
                                    modifier = Modifier.size(25.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Button(onClick = onRefresh) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh",
                                    modifier = Modifier.size(25.dp),
                                )
                            }
                        }

                    }
                }
            }
        }
    }

}
