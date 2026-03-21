package com.raibbl.ayabelquran.presentation.pages

import MediaPlayer
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.components.AnimatedSwipeHint
import com.raibbl.ayabelquran.presentation.navigation.Screen
import com.raibbl.ayabelquran.presentation.theme.AppThemeColors
import com.raibbl.ayabelquran.presentation.theme.AppThemeShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearMaterialApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun SurahAudioPage(
    navController: NavHostController
) {
    val listState = rememberScalingLazyListState()
    val surahs = stringArrayResource(id = R.array.surah_array)
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val focusRequester = rememberActiveFocusRequester()
    val coroutineScope = rememberCoroutineScope()
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { -400.dp.toPx() } to 1,
    )
    val context = LocalContext.current
    val activeSurahId = remember { mutableStateOf<Int?>(null) }
    val loadingSurahId = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue == 1) {
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.MainScreen.route) {
                    inclusive = true
                }
            }

        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .onRotaryScrollEvent {
                        coroutineScope.launch {
                            listState.scrollBy(it.verticalScrollPixels)
                        }
                        true
                    }
                    .swipeable(
                        state = swipeableState,
                        anchors = anchors,
                        thresholds = { _, _ -> FractionalThreshold(0.3f) },
                        orientation = Orientation.Horizontal
                    )
                    .focusRequester(focusRequester)
                    .focusable(),
                state = listState,

                ) {

                surahs.forEachIndexed { index, surahName ->
                    val currentSurahId = index + 1
                    item(key = "surah_audio_item_$currentSurahId") {
                        SurahPlayItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            text = surahName,
                            isActive = activeSurahId.value == currentSurahId,
                            isLoading = loadingSurahId.value == currentSurahId,
                            onClick = {
                                val surahUrl =
                                    "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/${currentSurahId}.mp3"
                                if (!MediaPlayer.isInitializedWithSource(surahUrl)) {
                                    loadingSurahId.value = currentSurahId
                                    MediaPlayer.initializeMediaPlayer(
                                        surahUrl,
                                        surahName,
                                        context,
                                        onReady = {
                                            loadingSurahId.value = null
                                            activeSurahId.value = currentSurahId
                                            MediaPlayer.playPause(context)
                                        },
                                        onCompletion = {
                                            if (activeSurahId.value == currentSurahId) {
                                                activeSurahId.value = null
                                            }
                                        }
                                    )
                                } else {
                                    activeSurahId.value =
                                        if (activeSurahId.value == currentSurahId) null else currentSurahId
                                    MediaPlayer.playPause(context)
                                }
                            }
                        )
                    }
                }
            }
            AnimatedSwipeHint(
                direction = "right",
                modifier = Modifier.align(Alignment.CenterEnd),
                animateOnEntry = true
            )
        }

    }
}
@Composable
fun SurahPlayItem(
    modifier: Modifier = Modifier,
    text: String,
    isActive: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .height(58.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppThemeColors.Primary,
            contentColor = AppThemeColors.OnPrimary
        ),
        shape = AppThemeShapes.Pill,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when {
                    isLoading -> Icons.Default.HourglassEmpty
                    isActive -> Icons.Filled.Pause
                    else -> Icons.Filled.PlayArrow
                },
                contentDescription = when {
                    isLoading -> "Loading"
                    isActive -> "Pause"
                    else -> "Play"
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center
            )
        }
    }
}



@Preview
@Composable
fun PreviewSurahAudioPage() {
    // Mocked NavHostController for the preview
    val navController = NavHostController(LocalContext.current).apply {
        navigatorProvider.addNavigator(
            ComposeNavigator()
        )
    }


    // Display the page in the preview
    SurahAudioPage(
        navController = navController
    )
}
