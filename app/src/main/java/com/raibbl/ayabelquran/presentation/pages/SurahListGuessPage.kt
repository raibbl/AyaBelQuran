package com.raibbl.ayabelquran.presentation.pages

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.ComposeNavigator
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.navigation.Screen
import com.raibbl.ayabelquran.presentation.theme.AppThemeColors
import com.raibbl.ayabelquran.presentation.theme.AppThemeShapes
import kotlinx.coroutines.launch
@OptIn(ExperimentalWearMaterialApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun SurahListGuessPage(
    surahId: Int,
    navController: NavHostController
) {
    val listState = rememberScalingLazyListState()
    val surahs = stringArrayResource(id = R.array.surah_array)
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val focusRequester = rememberActiveFocusRequester()
    val coroutineScope = rememberCoroutineScope()
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { 400.dp.toPx() } to 1 // Swipe to the right
    )
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
                item(key = "surah_guess_item_$currentSurahId") {
                    TextItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        text = surahName,
                        onClick = {
                            if (surahId == currentSurahId) {
                                navController.navigate("${Screen.surahGuessAnswerScreen.route}/true")
                                Log.d(
                                    "surahItem",
                                    "SurahListGuessPage: correct $surahName pressed id $currentSurahId"
                                )
                            } else {
                                try {
                                    navController.navigate("${Screen.surahGuessAnswerScreen.route}/false")
                                } catch (e: Exception) {
                                    Log.e("NavigationError", "Error navigating: ${e.message}")
                                }

                                Log.d(
                                    "surahItem",
                                    "SurahListGuessPage: false $surahName pressed id $currentSurahId, correct$surahId"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TextItem(modifier: Modifier, text: String, onClick: () -> Unit) {
    Button(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppThemeColors.Primary,
            contentColor = AppThemeColors.OnPrimary
        ),
        shape = AppThemeShapes.Pill,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 16.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
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
