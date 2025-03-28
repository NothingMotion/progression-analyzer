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
        const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val SIMPLE_DATE_FORMAT = "yyyy-MM-dd"

        // Persian (Jalali) calendar formats
        const val PERSIAN_FORMAT = "Y/m/d"
        const val PERSIAN_FULL_FORMAT = "Y/m/d H:i:s"



        private val jalaliDateUtils = JalaliDateUtils
        /**
         * Converts a Date to a formatted string using default format
         * @param date The date to format
         * @return Formatted date string
         */
        fun toFormatted(date: Date,format: String = DEFAULT_FORMAT): String {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
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
         * @param format The format to use (defaults to PERSIAN_FORMAT)
         * @return Formatted Persian date string
         */
        fun toPersianFormatted(date: Date, format: String = PERSIAN_FORMAT,convertToPersian: Boolean = true): String {
            return jalaliDateUtils.dateToJalali(date,format,convertToPersian)
        }

        /**
         * Converts Gregorian date to Jalali (Persian) date
         * @param gregorianYear Gregorian year
         * @param gregorianMonth Gregorian month (1-12)
         * @param gregorianDay Gregorian day
         * @return Array of [year, month, day] in Jalali calendar
         */
        fun toJalali(gregorianYear: Int, gregorianMonth: Int, gregorianDay: Int): IntArray {
           return jalaliDateUtils.gregorianToJalali(gregorianYear,gregorianMonth,gregorianDay)
        }

        /**
         * Converts Jalali (Persian) date to Gregorian date
         * @param jalaliYear Jalali year
         * @param jalaliMonth Jalali month (1-12)
         * @param jalaliDay Jalali day
         * @return Array of [year, month, day] in Gregorian calendar
         */
        fun toGregorian(jalaliYear: Int, jalaliMonth: Int, jalaliDay: Int): IntArray {
           return jalaliDateUtils.jalaliToGregorian(jalaliYear,jalaliMonth,jalaliDay)
        }

        /**
         * Checks if a given Jalali year is a leap year
         * @param year Jalali year
         * @return True if the year is a leap year, false otherwise
         */
        fun isJalaliLeapYear(year: Int): Boolean {
         return jalaliDateUtils.isJalaliLeapYear(year)
        }

        /**
         * Converts Gregorian date to Julian day number
         */

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