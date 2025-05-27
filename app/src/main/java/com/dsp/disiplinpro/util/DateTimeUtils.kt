package com.dsp.disiplinpro.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale("id"))
    private val dateTimeFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id"))

    fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }

    fun formatTime(date: Date): String {
        return timeFormatter.format(date)
    }

    fun formatDateTime(date: Date): String {
        return dateTimeFormatter.format(date)
    }

    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isTomorrow(date: Date, today: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date }
        val cal2 = Calendar.getInstance().apply { time = today }
        cal2.add(Calendar.DAY_OF_YEAR, 1)
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isInSameWeek(date: Date, today: Date): Boolean {
        val cal1 = Calendar.getInstance().apply {
            time = date
            firstDayOfWeek = Calendar.MONDAY
        }
        val cal2 = Calendar.getInstance().apply {
            time = today
            firstDayOfWeek = Calendar.MONDAY
        }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    fun calculateDaysLate(deadline: Date, today: Date): Int {
        val diff = today.time - deadline.time
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    fun getRelativeDeadline(deadline: Date, today: Date): String {
        if (isTomorrow(deadline, today)) return "besok"

        val diffInMillis = deadline.time - today.time
        val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

        return when {
            diffInDays < 7 -> "$diffInDays hari lagi"
            diffInDays < 14 -> "1 minggu lagi"
            diffInDays < 30 -> "${diffInDays / 7} minggu lagi"
            diffInDays < 60 -> "1 bulan lagi"
            else -> "${diffInDays / 30} bulan lagi"
        }
    }

    fun getTimeAgo(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "baru saja"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)} menit yang lalu"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)} jam yang lalu"
            diff < TimeUnit.DAYS.toMillis(2) -> "kemarin"
            diff < TimeUnit.DAYS.toMillis(7) -> "${diff / TimeUnit.DAYS.toMillis(1)} hari yang lalu"
            else -> dateFormatter.format(date)
        }
    }

    fun getDayOrder(day: String): Int {
        return when (day.lowercase()) {
            "senin" -> 1
            "selasa" -> 2
            "rabu" -> 3
            "kamis" -> 4
            "jumat" -> 5
            "sabtu" -> 6
            "minggu" -> 7
            else -> 8
        }
    }

    fun getCurrentDayName(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            Calendar.SUNDAY -> "Minggu"
            else -> ""
        }
    }
}