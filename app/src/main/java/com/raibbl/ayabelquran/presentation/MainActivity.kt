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
    companion object {
        /** Global verse number (1..6236) to open on, e.g. from the watch face complication. */
        const val EXTRA_VERSE_NUMBER = "com.raibbl.ayabelquran.extra.VERSE_NUMBER"
    }

    // Override onDestroy to release the MediaPlayer when the Activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
//        MediaPlayer.releasePlayer(this)
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

        val requestedVerse = intent.getIntExtra(EXTRA_VERSE_NUMBER, 0).takeIf { it in 1..6236 }
        VerseData.fetchVerseData(this, responseString, verseNumber, verseTafsir, false, requestedVerse)

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


















