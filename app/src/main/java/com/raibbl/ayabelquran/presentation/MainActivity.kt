/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.raibbl.ayabelquran.presentation

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.theme.AyaBelQuranTheme
import org.json.JSONObject


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
// ...

// Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.alquran.cloud/v1/ayah/262/editions/quran-uthmani,en.asad"

// Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val obj = JSONObject(response)

                responseString.value  = obj.getJSONArray("data").getJSONObject(0).getString("text")
            },
            { responseString.value  = "That didn't work!" })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
        setContent {
            WearApp(responseString.value,"https://cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3")
        }
    }
}



@Composable
fun WearApp(ayaText: String,ayaAudioUrl: String) {
    AyaBelQuranTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Aya(ayaText = ayaText, ayaAudioUrl =ayaAudioUrl )
        }
    }
}

@Composable
fun Aya(ayaText: String,ayaAudioUrl:String) {
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
                    .padding(start=30.dp,end=30.dp,top=35.dp,bottom=40.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.primary
            )
        }

        // Navigation Bar at the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(35.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            PlayButton {
                playAudioFromUrl(ayaAudioUrl)
            }
        }
    }

}

@Composable
fun PlayButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
//                Playmodifier = Modifier.padding(end = 8.dp)
            )
            Text("ترتيل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}






var mediaPlayer: MediaPlayer? = null

fun playAudioFromUrl(url: String) {
    if (mediaPlayer == null) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepareAsync() // Might take long! Use prepare() for local files.
            setOnPreparedListener { start() }
        }
    } else {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
    }
}

// Don't forget to release the MediaPlayer when done
fun releaseMediaPlayer() {
    mediaPlayer?.release()
    mediaPlayer = null
}


