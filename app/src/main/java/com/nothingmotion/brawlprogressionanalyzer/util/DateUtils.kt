package com.nothingmotion.brawlprogressionanalyzer.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateUtils {
    companion object {
        // Standard date formats
        private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"
        private const val SIMPLE_DATE_FORMAT = "yyyy-MM-dd"

        // Persian (Jalali) calendar formats
        private const val PERSIAN_FORMAT = "yyyy/MM/dd"
        private const val PERSIAN_FULL_FORMAT = "yyyy/MM/dd HH:mm:ss"

        /**
         * Converts a Date to a formatted string using default format
         * @param date The date to format
         * @return Formatted date string
         */
        fun toFormatted(date: Date): String {
            val sdf = SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault())
            return sdf.format(date)
        }

        /**
         * Returns current date and time as a formatted string
         * @return Current date and time in default format
         */
        fun nowFormatted(): String {
            return toFormatted(now())
        }

        /**
         * Returns current date and time
         * @return Current Date object
         */
        fun now(): Date {
            return Date()
        }

        /**
         * Converts a Date to a Persian (Jalali) formatted string
         * @param date The date to convert
         * @return Formatted Persian date string
         */
        fun toPersianFormatted(date: Date): String {
           return ""
        }


        /**
         * Additional utility methods for flexible date handling
         */

        /**
         * Format date with a custom format
         * @param date Date to format
         * @param format Custom format string
         * @return Formatted date string
         */
        fun formatDate(date: Date, format: String = DEFAULT_FORMAT): String {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            return sdf.format(date)
        }

        /**
         * Parse a date string to Date object
         * @param dateString Date string to parse
         * @param format Format of the input date string
         * @return Parsed Date object
         */
        fun parseDate(dateString: String, format: String = DEFAULT_FORMAT): Date {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            return sdf.parse(dateString) ?: Date()
        }
    }
}