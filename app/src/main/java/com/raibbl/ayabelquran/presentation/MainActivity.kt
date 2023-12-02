
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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        fetchVerseData(this, responseString, verseNumber, false)
        setContent {
            WearApp(
                responseString.value,
                "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${verseNumber.intValue}.mp3",
                onRefresh = {
                    fetchVerseData(this, responseString, verseNumber, true)
                })
        }
    }
}



@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun WearApp(ayaText: String, ayaAudioUrl: String, onRefresh: () -> Unit) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(0f to 0, with(LocalDensity.current){-400.dp.toPx()} to 1) // Adjust the position value as needed


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            ),
        contentAlignment = Alignment.Center
    ) {
        if (swipeableState.currentValue == 0) {
            AyaPage(ayaText = ayaText, ayaAudioUrl = ayaAudioUrl, onRefresh)
        } else {
            SecondPage()
        }
    }
}


@Composable
fun AyaPage(ayaText: String, ayaAudioUrl: String, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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

@Composable
fun SecondPage() {
    Text("hey")
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

fun generateDailyVerseUrl(randomize: Boolean): String {
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
    return "https://api.alquran.cloud/v1/ayah/$verseNumber/editions/quran-uthmani,en.asad"
}

fun fetchVerseData(
    context: Context,
    responseString: MutableState<String>,
    verseNumber: MutableState<Int>,
    randomize: Boolean
) {
    val queue = Volley.newRequestQueue(context)
    val url = generateDailyVerseUrl(randomize)

    val stringRequest = StringRequest(
        Request.Method.GET, url,
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

    queue.add(stringRequest)
}