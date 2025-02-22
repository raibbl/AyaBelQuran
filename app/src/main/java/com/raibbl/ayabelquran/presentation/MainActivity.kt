package com.raibbl.ayabelquran.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import api.VerseData
import com.raibbl.ayabelquran.presentation.navigation.NavigationHost
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    // Override onDestroy to release the MediaPlayer when the Activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        MediaPlayer.releasePlayer(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val responseString = mutableStateOf("Loading...")
        val verseNumber = mutableIntStateOf(0)
        val verseTafsir =
            mutableStateOf(JSONObject("""{"surah":{"name":"Test Surah Name"},text:"kk",numberInSurah:266}"""))

        VerseData.fetchVerseData(this, responseString, verseNumber, verseTafsir, false)

        setContent {
            NavigationHost(
                responseString.value,
                onRefresh = {
                    VerseData.fetchVerseData(this, responseString, verseNumber, verseTafsir, true)
                }, verseTafsir.value
            )

        }
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle the incoming intent if needed
    }
}


















