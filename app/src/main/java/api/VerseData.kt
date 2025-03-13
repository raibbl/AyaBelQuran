package api

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import utilities.generateVerseNumber

class VerseData {


    companion object {
        fun fetchVerseData(
            context: Context,
            responseString: MutableState<String>,
            verseNumber: MutableState<Int>,
            verseTafsir: MutableState<JSONObject>,
            randomize: Boolean
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val queue = Volley.newRequestQueue(context)
                val generatedVerseNumber = generateVerseNumber(randomize)
                val verseRequestUrl =
                    "https://api.alquran.cloud/v1/ayah/$generatedVerseNumber/editions/quran-uthmani,en.asad"

                val verseRequest = StringRequest(
                    Request.Method.GET, verseRequestUrl,
                    { response ->
                        try {
                            val obj = JSONObject(response)
                            val verseText =
                                obj.getJSONArray("data").getJSONObject(0).getString("text")
                            val verseNum =
                                obj.getJSONArray("data").getJSONObject(0).getInt("number")
                            println(obj.getJSONArray("data").toString())
                            val surahName = obj.getJSONArray("data").getJSONObject(0).getJSONObject("surah").getString("englishName")
                            val verseInSurah = obj.getJSONArray("data").getJSONObject(0).getInt("numberInSurah")
                            responseString.value = verseText
                            verseNumber.value = verseNum
//                            val title = "Surah $surahName, Verse $verseInSurah"
////                            MediaPlayer.initializeMediaPlayer("https://cdn.islamic.network/quran/audio/128/ar.alafasy/${generatedVerseNumber}.mp3",
////                                title,context)
////                            // Debugging log
                            Log.d(
                                "fetchVerseData",
                                "Verse number: $verseNum, Verse text: $verseText"
                            )
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
        }

        /**
         * Fetches all Ayahs from a Surah and returns them as a list of (text, audio URL).
         */
        fun fetchSurahAyahs(
            context: Context,
            surahId: Int,
            onComplete: (List<Pair<String, String>>?) -> Unit
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val queue = Volley.newRequestQueue(context)
                val surahRequestUrl = "https://api.alquran.cloud/v1/surah/$surahId/ar.alafasy"

                val surahRequest = StringRequest(
                    Request.Method.GET, surahRequestUrl,
                    { response ->
                        try {
                            val obj = JSONObject(response)
                            val ayahs = obj.getJSONObject("data").getJSONArray("ayahs")

                            val ayahList = mutableListOf<Pair<String, String>>() // (text, audio)
                            for (i in 0 until ayahs.length()) {
                                val ayahObj = ayahs.getJSONObject(i)
                                val ayahText = ayahObj.getString("text")
                                val ayahAudio = ayahObj.getString("audio")
                                ayahList.add(Pair(ayahText, ayahAudio))
                            }

                            onComplete(ayahList) // ✅ Pass result back
                            Log.d("fetchSurahAyahs", "Fetched ${ayahList.size} ayahs for Surah $surahId")
                        } catch (e: Exception) {
                            Log.e("fetchSurahAyahs", "Error parsing data: ${e.message}")
                            onComplete(null) // ✅ Return null if error
                        }
                    },
                    {
                        Log.e("fetchSurahAyahs", "Request failed for Surah $surahId")
                        onComplete(null) // ✅ Return null if request fails
                    }
                )

                queue.add(surahRequest)
            }
        }
    }
}