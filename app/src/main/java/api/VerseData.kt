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
import utilities.MediaPlayer
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
                MediaPlayer.initializeMediaPlayer("https://cdn.islamic.network/quran/audio/64/ar.alafasy/${generatedVerseNumber}.mp3")
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

                            responseString.value = verseText
                            verseNumber.value = verseNum

                            // Debugging log
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
    }
}