package com.raibbl.ayabelquran.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import api.VerseData
import com.raibbl.ayabelquran.presentation.navigation.NavigationHost
import org.json.JSONObject
import utilities.MediaPlayer


class MainActivity : ComponentActivity() {
    // Override onDestroy to release the MediaPlayer when the Activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        MediaPlayer.releaseMediaPlayer()
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
                "https://cdn.islamic.network/quran/audio/64/ar.alafasy/${verseNumber.intValue}.mp3",
                onRefresh = {
                    VerseData.fetchVerseData(this, responseString, verseNumber, verseTafsir, true)
                }, verseTafsir.value
            )

        }
    }
}


















