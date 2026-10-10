package com.raibbl.ayabelquran.complication

import android.content.Context
import com.raibbl.ayabelquran.R
import java.time.LocalDate

data class GlanceVerse(
    val number: Int,
    val surah: Int,
    val ayah: Int,
    val surahName: String,
    val text: String
) {
    val reference: String get() = "${surahName.uppercase()} · $surah:$ayah"
}

/**
 * Verses short enough to show whole on a watch face, bundled so the
 * complication works offline and never hits the network.
 * Generated from the alquran.cloud quran-uthmani edition (Tanzil text).
 */
object GlanceVerses {
    // Prime stride: walks every verse once before repeating, while
    // consecutive days land in different surahs.
    private const val STRIDE = 7919L

    @Volatile
    private var verses: List<GlanceVerse>? = null

    fun all(context: Context): List<GlanceVerse> = verses ?: synchronized(this) {
        verses ?: load(context).also { verses = it }
    }

    fun forDate(context: Context, date: LocalDate = LocalDate.now()): GlanceVerse {
        val list = all(context)
        val index = Math.floorMod(date.toEpochDay() * STRIDE, list.size.toLong()).toInt()
        return list[index]
    }

    private fun load(context: Context): List<GlanceVerse> =
        context.resources.openRawResource(R.raw.glance_verses).bufferedReader(Charsets.UTF_8)
            .useLines { lines ->
                lines.filter { it.isNotBlank() }.map { line ->
                    val (number, surah, ayah, name, text) = line.split('\t', limit = 5)
                    GlanceVerse(number.toInt(), surah.toInt(), ayah.toInt(), name, text)
                }.toList()
            }
}
