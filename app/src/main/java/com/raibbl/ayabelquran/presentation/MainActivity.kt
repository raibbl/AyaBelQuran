package com.raibbl.ayabelquran.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.Calendar
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Random


class MainActivity : ComponentActivity() {
    // Override onDestroy to release the MediaPlayer when the Activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val responseString = mutableStateOf("Loading...")
        val verseNumber = mutableIntStateOf(0)
        val verseTafsir = mutableStateOf(JSONObject("""{"surah":{"name":"Test Surah Name"},text:"kk",numberInSurah:266}"""))

        fetchVerseData(this, responseString, verseNumber, verseTafsir, false)
        setContent {
            WearApp(
                responseString.value,
                "https://cdn.islamic.network/quran/audio/64/ar.alafasy/${verseNumber.intValue}.mp3",
                onRefresh = {
                    fetchVerseData(this, responseString, verseNumber, verseTafsir, true)
                }, verseTafsir.value
            )

        }
    }
}
sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")

    object tafsirPage : Screen("tafsirPage_screen")
    object SwipeUpScreen : Screen("swipe_up_screen")
}


enum class PanelState { Hidden, Visible }
@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun WearApp(
    ayaText: String,
    ayaAudioUrl: String,
    onRefresh: () -> Unit,
    verseTafsir: JSONObject,
) {
    val navController = rememberNavController()

    // Navigation Host
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            AyaPage(
                ayaText = ayaText,
                ayaAudioUrl = ayaAudioUrl,
                onRefresh = onRefresh,
                navController = navController
            )
        }

        composable(Screen.tafsirPage.route) {
            TafsirPage(verseTafsir,navController)
        }
    }
}












@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun AyaPage(ayaText: String, ayaAudioUrl: String, onRefresh: () -> Unit,navController:NavHostController) {
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


            IconButton(onClick = { playAudioFromUrl(ayaAudioUrl) }) {
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

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun TafsirPage(verseTafsir: JSONObject, navController:NavHostController) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(
        0f to 0,
        with(LocalDensity.current) { 400.dp.toPx() } to 1 // Swipe to the right
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

        if (swipeableState.currentValue==1){
            LaunchedEffect(Unit) {
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
                }

            }
        }
        AnimatedSwipeHint(direction = "left")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "${verseTafsir?.getJSONObject("surah")?.getString("name")} أية-${verseTafsir?.getInt("numberInSurah")
                    ?.let { convertToArabicNumbers(it) }}",
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 35.dp, bottom = 15.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.primary
            )
            Text(
                text = verseTafsir.getString("text"),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 0.dp, bottom = 40.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.primary
            )
        }
    }
}


var mediaPlayer: MediaPlayer? = null

fun playAudioFromUrl(url: String) {
    if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
        mediaPlayer?.release() // Release any previous MediaPlayer
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepareAsync() // Prepare the MediaPlayer asynchronously
            setOnPreparedListener { start() }
        }
    } else {
        mediaPlayer?.pause()
    }
}

// Don't forget to release the MediaPlayer when done
fun releaseMediaPlayer() {
    mediaPlayer?.release()
    mediaPlayer = null
}

fun generateVerseNumber(randomize: Boolean): Int {
    val verseNumber: Int = if (randomize) {
        // Completely random verse number
        Random().nextInt(6236) + 1
    } else {
        // Random verse number based on the current day and year
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        val seed = year * 365 + dayOfMonth
        Random(seed.toLong()).nextInt(6236) + 1
    }

    // Construct the URL with the generated verse number
    return verseNumber
}

fun fetchVerseData(
    context: Context,
    responseString: MutableState<String>,
    verseNumber: MutableState<Int>,
    verseTafsir: MutableState<JSONObject>,
    randomize: Boolean
) {
    val queue = Volley.newRequestQueue(context)
    val generatedVerseNumber = generateVerseNumber(randomize)
    val verseRequestUrl =
        "https://api.alquran.cloud/v1/ayah/$generatedVerseNumber/editions/quran-uthmani,en.asad"

    val verseRequest = StringRequest(
        Request.Method.GET, verseRequestUrl,
        { response ->
            try {
                val obj = JSONObject(response)
                val verseText = obj.getJSONArray("data").getJSONObject(0).getString("text")
                val verseNum = obj.getJSONArray("data").getJSONObject(0).getInt("number")

                responseString.value = verseText
                verseNumber.value = verseNum

                // Debugging log
                Log.d("fetchVerseData", "Verse number: $verseNum, Verse text: $verseText")
            } catch (e: Exception) {
                responseString.value = "Error parsing data!"
                Log.e("fetchVerseData", "Error: ${e.message}")
            }
        },
        {
            responseString.value = "That didn't work!"
            Log.e("fetchVerseData", "Request failed")
        }
    )
    val verseTafsirRequestUrl =
        "https://api.alquran.cloud/v1/ayah/$generatedVerseNumber/ar.muyassar"
    val verseTafsirRequest = StringRequest(
        Request.Method.GET, verseTafsirRequestUrl,
        { response ->
            try {
                val obj = JSONObject(response)
                val verseTafsirObject = obj.getJSONObject("data")
                verseTafsir.value = verseTafsirObject

            } catch (e: Exception) {
                responseString.value = "Error parsing data!"
                Log.e("fetchVerseData", "Error: ${e.message}")
            }
        },
        {
            responseString.value = "That didn't work!"
            Log.e("fetchVerseData", "Request failed")
        }
    )

    queue.add(verseRequest)
    queue.add(verseTafsirRequest)
}

fun convertToArabicNumbers(num: Number): String {
    val arabicNumbers = listOf('\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668', '\u0669')
    return num.toString().map { digit ->
        if (digit.isDigit()) {
            arabicNumbers[digit.toString().toInt()]
        } else {
            digit
        }
    }.joinToString("")
}

@Composable
fun AnimatedSwipeHint(direction: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f, // Adjust the distance
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animation offset"
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = if (direction == "right") Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Icon(
            imageVector = if (direction == "right") Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
            contentDescription = "Swipe",
            modifier = Modifier
                .offset(x = offsetX.dp)
                .padding(end = 16.dp)
        )
    }
}
