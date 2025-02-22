package com.raibbl.ayabelquran.presentation.pages

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.raibbl.ayabelquran.presentation.components.AnimatedScrollHint
import com.raibbl.ayabelquran.presentation.navigation.Screen
import com.raibbl.ayabelquran.presentation.components.AnimatedSwipeHint
import kotlinx.coroutines.launch
import org.json.JSONObject
import utilities.convertToArabicNumbers

@OptIn(ExperimentalWearMaterialApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun TafsirPage(verseTafsir: JSONObject, navController: NavHostController) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val listState = rememberScalingLazyListState()
    val focusRequester = rememberActiveFocusRequester()
    val coroutineScope = rememberCoroutineScope()
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { 400.dp.toPx() } to 1 // Swipe to the right
    )


    Scaffold (      positionIndicator = {
        androidx.wear.compose.material.PositionIndicator(
            scalingLazyListState = listState
        )
    } ){
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
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.MainScreen.route) {
                            inclusive = true
                        }
                    }

                }
            }
            AnimatedSwipeHint(direction = "left")
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .onRotaryScrollEvent {
                        coroutineScope.launch {
                            listState.scrollBy(it.verticalScrollPixels)

                            listState.animateScrollBy(0f)
                        }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                  state = listState
            ) {
                item {
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    Text(
                        text = "${verseTafsir.getJSONObject("surah").getString("name")} أية-${
                            verseTafsir.getInt("numberInSurah")
                                ?.let { convertToArabicNumbers(it) }
                        }",
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 30.dp, end = 30.dp, top = 35.dp, bottom = 15.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colors.primary
                    )
                }
                item {
                    Text(
                        text = verseTafsir.getString("text"),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .padding(start = 30.dp, end = 30.dp, bottom = 40.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}