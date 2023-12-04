package com.raibbl.ayabelquran.presentation.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")

    object tafsirPage : Screen("tafsirPage_screen")
    object surahListGuessScreen : Screen("surah_listGuess_screen")
}