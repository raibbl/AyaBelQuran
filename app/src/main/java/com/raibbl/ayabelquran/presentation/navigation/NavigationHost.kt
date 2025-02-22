package com.raibbl.ayabelquran.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raibbl.ayabelquran.presentation.pages.AyaPage
import com.raibbl.ayabelquran.presentation.pages.SurahAudioPage
import com.raibbl.ayabelquran.presentation.pages.SurahGuessAnswerPage
import com.raibbl.ayabelquran.presentation.pages.SurahListGuessPage
import com.raibbl.ayabelquran.presentation.pages.TafsirPage
import org.json.JSONObject


@Composable
fun NavigationHost(
    ayaText: String,
    onRefresh: () -> Unit,
    verseTafsir: JSONObject,
) {
    val navController = rememberNavController()


    // Navigation Host
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        val verseNumber: Int = verseTafsir.optInt("number", -1)
        composable(Screen.MainScreen.route) {
            AyaPage(
                ayaText = ayaText,
                onRefresh = onRefresh,
                navController = navController,
                verseNumber = verseNumber
            )
        }

        composable(Screen.tafsirPage.route) {
            TafsirPage(verseTafsir, navController)
        }

        composable(Screen.surahListGuessScreen.route) {
            val surahNumber: Int = verseTafsir.getJSONObject("surah").optInt("number",0)
            SurahListGuessPage(surahNumber, navController)
        }

        composable("${Screen.surahGuessAnswerScreen.route}/{isCorrect}") { backStackEntry ->
            val isCorrectString = backStackEntry.arguments?.getString("isCorrect") ?: "false"
            val isCorrect = isCorrectString.toBoolean()

            SurahGuessAnswerPage(isCorrect,navController)
        }

        composable(Screen.surahAudioPageScreen.route) {
            SurahAudioPage(navController)
        }

    }
}