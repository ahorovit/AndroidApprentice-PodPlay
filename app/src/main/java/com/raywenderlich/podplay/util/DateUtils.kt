package com.raywenderlich.podplay.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun dateToShortDate(date: Date): String {
        val outputFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
        return outputFormat.format(date)
    }

    fun jsonDateToShortDate(jsonDate: String?) : String {
        if (jsonDate == null){
            return "-"
        }

        val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inFormat.parse(jsonDate) ?: return "-"

        val outFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

        return outFormat.format(date)
    }

    fun xmlDateToDate(dateString: String?): Date {
        val date = dateString?: return Date()
        val inFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault())
        return inFormat.parse(date) ?: Date()
    }
}