package com.raibbl.ayabelquran.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raibbl.ayabelquran.presentation.pages.AyaPage
import com.raibbl.ayabelquran.presentation.pages.SurahListGuessPage
import com.raibbl.ayabelquran.presentation.pages.TafsirPage
import org.json.JSONObject


@Composable
fun NavigationHost(
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
                onRefresh = onRefresh,
                navController = navController
            )
        }

        composable(Screen.tafsirPage.route) {
            TafsirPage(verseTafsir, navController)
        }

        composable(Screen.surahListGuessScreen.route) {
            SurahListGuessPage(verseTafsir.getJSONObject("surah").getInt("number"), navController)
        }
    }
}