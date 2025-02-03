package com.raibbl.ayabelquran.presentation.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object tafsirPage : Screen("tafsirPage_screen")
    object surahListGuessScreen : Screen("surah_listGuess_screen")
    object surahGuessAnswerScreen : Screen("surah_guessAnswer_screen")
    object surahAudioPageScreen : Screen("Surah_AudioPlayPage_screen")

}