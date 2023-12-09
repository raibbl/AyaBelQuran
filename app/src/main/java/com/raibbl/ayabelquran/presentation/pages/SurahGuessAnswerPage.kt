package com.raibbl.ayabelquran.presentation.pages


import androidx.compose.material3.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.raibbl.ayabelquran.R.drawable.correct_guess
import com.raibbl.ayabelquran.R.drawable.wrong_guess
import com.raibbl.ayabelquran.presentation.components.AnimatedSwipeHint
import com.raibbl.ayabelquran.presentation.navigation.Screen
import kotlinx.coroutines.launch


@OptIn(ExperimentalWearFoundationApi::class, ExperimentalWearMaterialApi::class)
@Composable
fun SurahGuessAnswerPage(isCorrect:Boolean , navController: NavHostController) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val verticalScrollState = rememberScrollState()
    val focusRequester = rememberActiveFocusRequester()
    val coroutineScope = rememberCoroutineScope()
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { 400.dp.toPx() } to 1 // Swipe to the right
    )
    val image = painterResource(id = if (isCorrect) correct_guess else wrong_guess)
    Box(modifier = Modifier.fillMaxSize()
        .swipeable(
            state = swipeableState,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.3f) },
            orientation = Orientation.Horizontal
        ), contentAlignment = Alignment.Center) {
        AnimatedSwipeHint(direction = "left")
        if (swipeableState.currentValue == 1) {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
                }

            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        verticalScrollState.scrollBy(it.verticalScrollPixels)

                        verticalScrollState.animateScrollBy(0f)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable()
                .align(Alignment.TopCenter)
                .verticalScroll(verticalScrollState)
        ) {
            Image(
                painter = image,
                contentDescription = "correctOrBadGuessImage", // Provide a meaningful description
               colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                        modifier = Modifier
                            .size(width = 100.dp, height = 100.dp)
                        .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 2.dp).fillMaxSize()
                .align(Alignment.CenterHorizontally),
            )
            Text(
                text = if (isCorrect) "! صحيح" else "! خطأ",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 2.dp, bottom = 0.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.primary
            )
            Button(
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 5.dp, bottom = 20.dp)
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate(Screen.tafsirPage.route)
                    }

            ){
               Text(
                   text = "تعرف على الآية",
                   textAlign = TextAlign.Center,
                   fontSize = 12.sp,
                )
            }
    }
}}