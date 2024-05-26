package com.example.personalphysicaltracker

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.ENGLISH)
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.ENGLISH).let { value ->
        if (uppercase) value.uppercase(Locale.ENGLISH) else value
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

fun getWeekPageTitle(week: Week): String {
    val firstDate = week.days.first().date
    val lastDate = week.days.last().date
    return when {
        firstDate.yearMonth == lastDate.yearMonth -> {
            firstDate.yearMonth.displayText()
        }

        firstDate.year == lastDate.year -> {
            "${firstDate.month.displayText(short = false)} - ${lastDate.yearMonth.displayText()}"
        }

        else -> {
            "${firstDate.yearMonth.displayText()} - ${lastDate.yearMonth.displayText()}"
        }
    }
}

fun getYear(time: Long): String {
    val date = java.time.Instant.ofEpochMilli(time)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    return date.year.toString()
}

fun getMonth(time: Long): String {
    val date = java.time.Instant.ofEpochMilli(time)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    return date.monthValue.toString()
}

fun getDayNumber(time: Long): Int {
    val date = java.time.Instant.ofEpochMilli(time)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    return date.dayOfMonth
}

fun getDayActivities(
    activities: List<com.example.personalphysicaltracker.data.Activity>,
    date: LocalDate
): List<com.example.personalphysicaltracker.data.Activity> {
    //check if activity has been registered for the day
    //an activity is DISPLAYED IFF given day D and activity A, there is an entry in the database
    //s.t. A.startTime is in D and A.stopTime is in D OR A.startTime is in D-1 and A.stopTime is in D OR A.startTime is in D and A.stopTime is in D+1

    return activities.filter {
        val activityStartDate = it.startTime
        val activityEndDate = it.stopTime

        val selectedDateStartMillis =
            date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        val selectedDateEndMillis =
            date.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()

        (activityStartDate in selectedDateStartMillis until selectedDateEndMillis) ||
                (activityEndDate in selectedDateStartMillis until selectedDateEndMillis)
    }
}

internal fun Context.getDrawableCompat(@DrawableRes drawable: Int): Drawable =
    requireNotNull(ContextCompat.getDrawable(this, drawable))

internal fun Context.getColorCompat(@ColorRes color: Int) =
    ContextCompat.getColor(this, color)

internal fun TextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))

private fun timeStringFromLong(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60) % 60)
    val hours = (ms / (1000 * 60 * 60) % 24)
    return makeTimeString(hours, minutes, seconds)
}

private fun makeTimeString(hours: Long, minutes: Long, seconds: Long): String {
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}