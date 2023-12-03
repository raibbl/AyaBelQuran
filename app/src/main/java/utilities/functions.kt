package utilities

import android.icu.util.Calendar
import java.util.Random


fun convertToArabicNumbers(num: Number): String {
    val arabicNumbers = listOf(
        '\u0660',
        '\u0661',
        '\u0662',
        '\u0663',
        '\u0664',
        '\u0665',
        '\u0666',
        '\u0667',
        '\u0668',
        '\u0669'
    )
    return num.toString().map { digit ->
        if (digit.isDigit()) {
            arabicNumbers[digit.toString().toInt()]
        } else {
            digit
        }
    }.joinToString("")
}



fun generateVerseNumber(randomize: Boolean): Int {
    val verseNumber: Int = if (randomize) {
        // Completely random verse number
        Random().nextInt(6236) + 1
    } else {
        // Random verse number based on the current day and year
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        val seed = year * 365 + dayOfMonth
        Random(seed.toLong()).nextInt(6236) + 1
    }

    // Construct the URL with the generated verse number
    return verseNumber
}
