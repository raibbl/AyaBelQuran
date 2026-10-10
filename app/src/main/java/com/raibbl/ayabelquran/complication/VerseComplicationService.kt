package com.raibbl.ayabelquran.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.raibbl.ayabelquran.presentation.MainActivity

/**
 * "Verse of the Day" complication. LONG_TEXT carries the Arabic verse as text
 * and the surah reference as title; SHORT_TEXT carries just the reference.
 * Tapping opens the app on the same verse.
 */
class VerseComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        build(type, PREVIEW, tapAction = null)

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val verse = GlanceVerses.forDate(this)
        return build(request.complicationType, verse, tapAction(request.complicationInstanceId, verse))
    }

    private fun build(
        type: ComplicationType,
        verse: GlanceVerse,
        tapAction: PendingIntent?
    ): ComplicationData? {
        val description = PlainComplicationText.Builder("${verse.text} — ${verse.reference}").build()
        return when (type) {
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                PlainComplicationText.Builder(verse.text).build(),
                description
            )
                .setTitle(PlainComplicationText.Builder(verse.reference).build())
                .setTapAction(tapAction)
                .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                PlainComplicationText.Builder("${verse.surah}:${verse.ayah}").build(),
                description
            )
                .setTitle(PlainComplicationText.Builder("آية").build())
                .setTapAction(tapAction)
                .build()

            else -> null
        }
    }

    private fun tapAction(instanceId: Int, verse: GlanceVerse): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_VERSE_NUMBER, verse.number)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(
            this,
            instanceId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        val PREVIEW = GlanceVerse(6096, 94, 6, "Ash-Sharh", "إِنَّ مَعَ ٱلْعُسْرِ يُسْرًۭا")
    }
}
