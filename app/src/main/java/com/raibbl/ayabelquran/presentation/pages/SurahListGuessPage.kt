package com.raibbl.ayabelquran.presentation.pages

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.navigation.Screen
import kotlinx.coroutines.launch


@OptIn(ExperimentalWearFoundationApi::class, ExperimentalWearMaterialApi::class)
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
    if (swipeableState.currentValue == 1) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.MainScreen.route) {
                    inclusive = true
                }
            }

        }
    }
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
        contentPadding = PaddingValues(
            top = 10.dp,
            start = 10.dp,
            end = 10.dp,
            bottom = 40.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = listState
    ) {
        items(surahs.size) { index ->
            val curentSurahId = index + 1
            TextItem(
                modifier = Modifier.fillMaxWidth(),
                text = surahs[index],
                onClick = {
                    if (surahId == curentSurahId) {
                        // navigate to correct surah page with success
                        Log.d(
                            "surahItem",
                            "SurahListGuessPage: correct ${surahs[index]} pressed id ${curentSurahId}"
                        )
                    } else {
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

@Composable
fun TextItem(modifier: Modifier, text: String, onClick: () -> Unit) {
    Button(
        modifier = modifier.padding(8.dp),
        onClick = onClick
    ) {
        Text(text = text, style = TextStyle(fontSize = 16.sp), textAlign = TextAlign.Center)
    }
}
