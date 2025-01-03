package com.raibbl.ayabelquran.presentation.pages

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.ComposeNavigator
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.navigation.Screen
import kotlinx.coroutines.launch


@OptIn(ExperimentalWearMaterialApi::class, ExperimentalHorologistApi::class)
@Composable
fun SurahListGuessPage(
    surahId: Int,
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
        with(LocalDensity.current) { 400.dp.toPx() } to 1 // Swipe to the right
    )
    if (swipeableState.currentValue == 1) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.MainScreen.route) {
                    inclusive = true
                }
            }

        }
    }

    ScreenScaffold ( scrollState = listState ){
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
        columnState = listState,

    ) {
        items(surahs.size) { index ->
            val curentSurahId = index + 1
            TextItem(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                text = surahs[index],
                onClick = {
                    if (surahId == curentSurahId) {
                        // navigate to correct surah page with success
                        navController.navigate("${Screen.surahGuessAnswerScreen.route}/true")
                        Log.d(
                            "surahItem",
                            "SurahListGuessPage: correct ${surahs[index]} pressed id ${curentSurahId}"
                        )
                    } else {
                        try {
                            navController.navigate("${Screen.surahGuessAnswerScreen.route}/false")
                        } catch (e: Exception) {
                            Log.e("NavigationError", "Error navigating: ${e.message}")
                        }

                        Log.d(
                            "surahItem",
                            "SurahListGuessPage: false ${surahs[index]} pressed id ${curentSurahId}, correct${surahId}"
                        )
                    }
                }
            )
        }
    }
}
    }

@Composable
fun TextItem(modifier: Modifier, text: String, onClick: () -> Unit) {
    Button(
        modifier = modifier.padding(12.dp),
        onClick = onClick
    ) {
        Text(text = text, style = TextStyle(fontSize = 16.sp), textAlign = TextAlign.Center)
    }
}


@Preview
@Composable
fun PreviewSurahListGuessPage() {
    // Mocked NavHostController for the preview
    val navController = NavHostController(LocalContext.current).apply {
        navigatorProvider.addNavigator(
            ComposeNavigator()
        )
    }

    // Provide a previewable Surah ID
    val previewSurahId = 1

    // Display the page in the preview
    SurahListGuessPage(
        surahId = previewSurahId,
        navController = navController
    )
}
