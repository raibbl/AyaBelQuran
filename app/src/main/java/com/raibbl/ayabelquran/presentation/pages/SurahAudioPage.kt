package com.raibbl.ayabelquran.presentation.pages



import android.content.Context
import android.content.Intent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import api.VerseData
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.raibbl.ayabelquran.MediaPlaybackService
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.components.AnimatedSwipeHint
import com.raibbl.ayabelquran.presentation.navigation.Screen
import kotlinx.coroutines.launch

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

@Composable
fun SurahPlayItem(
    modifier: Modifier = Modifier,
    currentSurahId: Int,
    text: String,
    context: Context,
    activeSurahId: MutableState<Int?>,
    isPlaying: MutableState<Boolean>
) {
    val isLoading = rememberSaveable { mutableStateOf(false) }

    Button(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .height(50.dp),
        onClick = {
            if (activeSurahId.value != currentSurahId) {
                isLoading.value = true

                // ✅ Fetch and play Surah
                VerseData.fetchSurahAyahs(context, currentSurahId) { ayahList ->
                    isLoading.value = false
                    if (!ayahList.isNullOrEmpty()) {
                        activeSurahId.value = currentSurahId
                        isPlaying.value = true // ✅ Mark as playing

                        val ayahUrls = ayahList.map { it.second }
                        val intent = Intent(context, MediaPlaybackService::class.java).apply {
                            putStringArrayListExtra("PLAYLIST", ArrayList(ayahUrls))
                            putExtra("TITLE", text)
                            action = "PLAY"
                        }
                        context.startForegroundService(intent)
                    } else {
                        println("Failed to fetch Ayahs")
                    }
                }
            } else {
                // ✅ Toggle play/pause without losing state
                isPlaying.value = !isPlaying.value
                val toggleIntent = Intent(context, MediaPlaybackService::class.java).apply {
                    action = "TOGGLE_PLAY"
                }
                context.startService(toggleIntent)
            }
        }
    ) {
        Icon(
            imageVector = when {
                isLoading.value -> Icons.Default.HourglassEmpty
                isPlaying.value && activeSurahId.value == currentSurahId -> Icons.Filled.Pause
                else -> Icons.Filled.PlayArrow
            },
            contentDescription = if (isLoading.value) "Loading" else if (isPlaying.value) "Pause" else "Play",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalWearMaterialApi::class, ExperimentalHorologistApi::class)
@Composable
fun SurahAudioPage(
    navController: NavHostController
) {
    val listState = rememberResponsiveColumnState(
        first = ScalingLazyColumnDefaults.ItemType.Text,
        last = ScalingLazyColumnDefaults.ItemType.SingleButton,
        verticalArrangement = Arrangement.spacedBy(15.dp), // Adjust vertical spacing
        rotaryMode = ScalingLazyColumnState.RotaryMode.Scroll, // Enable rotary scrolling
        hapticsEnabled = true,
        reverseLayout = false,
        userScrollEnabled = true,
        initialItemIndex = 0
    )
    val surahs = stringArrayResource(id = R.array.surah_array)
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val focusRequester = rememberActiveFocusRequester()
    val coroutineScope = rememberCoroutineScope()
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { -200.dp.toPx() } to 1,
    )
    val context = LocalContext.current
    val activeSurahId = rememberSaveable { mutableStateOf<Int?>(null) }
    val isPlaying = rememberSaveable { mutableStateOf(false) }
    if (swipeableState.currentValue == 1) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.MainScreen.route) {
                    inclusive = true
                }
            }

        }
    }

    ScreenScaffold(scrollState = listState) {
        AnimatedSwipeHint(direction = "right")
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
                    thresholds = { _, _ -> FractionalThreshold(0.1f) }, // 🔥 Low
                    orientation = Orientation.Horizontal
                )
                .focusRequester(focusRequester)
                .focusable(),
            columnState = listState,

            ) {


            items(surahs.size) { index ->
                val currentSurahId = index + 1
                SurahPlayItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    currentSurahId = currentSurahId,
                    text = surahs[index],
                    context = LocalContext.current,
                    activeSurahId = activeSurahId,
                    isPlaying = isPlaying
                )
            }


        }
    }
}



