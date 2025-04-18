package com.nothingmotion.brawlprogressionanalyzer.util

import java.util.*

/**
 * Utility class for Jalali (Persian) date functionality
 * Ported from PHP jdate library
 */
class JalaliDateUtils {
    companion object {
        private val persianNumbers = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", ".")
        private val latinNumbers = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
        
        private val persianMonths = arrayOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
        
        private val persianDays = arrayOf(
            "یکشنبه", "دوشنبه", "سه شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
        )
        
        private val shortPersianMonths = arrayOf(
            "فر", "ار", "خر", "تی‍", "مر", "شه‍", "مه‍", "آب‍", "آذ", "دی", "به‍", "اس‍"
        )
        
        private val shortPersianDays = arrayOf("ی", "د", "س", "چ", "پ", "ج", "ش")
        
        private val seasons = arrayOf("بهار", "تابستان", "پاییز", "زمستان")
        
        /**
         * Main function to get Jalali date string with formatting
         * 
         * @param format The desired format string
         * @param timestamp Optional timestamp, defaults to current time
         * @param timezone Optional timezone, defaults to Asia/Tehran
         * @param convertToFarsi Whether to convert numbers to Persian, defaults to true
         * @return Formatted Jalali date string
         */
        fun jdate(
            format: String,
            timestamp: Long = System.currentTimeMillis() / 1000,
            timezone: String = "Asia/Tehran",
            convertToFarsi: Boolean = true
        ): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp * 1000
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            
            // Convert to Jalali
            val jalaliDate = gregorianToJalali(year, month, day)
            val jYear = jalaliDate[0]
            val jMonth = jalaliDate[1]
            val jDay = jalaliDate[2]
            
            // Calculate day of year
            val doy = if (jMonth < 7) {
                ((jMonth - 1) * 31) + jDay - 1
            } else {
                ((jMonth - 7) * 30) + jDay + 185
            }
            
            // Check if it's a leap year
            val kab = if (isJalaliLeapYear(jYear)) 1 else 0
            
            var result = ""
            var i = 0
            while (i < format.length) {
                var sub = format[i].toString()
                
                // Handle escaped characters
                if (sub == "\\") {
                    if (i + 1 < format.length) {
                        result += format[i + 1]
                        i += 2
                        continue
                    }
                }
                
                // Process format characters
                when (sub) {
                    // Standard date components
                    "d" -> result += if (jDay < 10) "0$jDay" else jDay.toString()
                    "D" -> result += shortPersianDays[dayOfWeek]
                    "j" -> result += jDay.toString()
                    "l" -> result += persianDays[dayOfWeek]
                    "N" -> result += (dayOfWeek + 1).toString()
                    "w" -> result += if (dayOfWeek == 6) "0" else (dayOfWeek + 1).toString()
                    "z" -> result += doy.toString()
                    
                    // Month
                    "F" -> result += persianMonths[jMonth - 1]
                    "m" -> result += if (jMonth > 9) jMonth.toString() else "0$jMonth"
                    "M" -> result += shortPersianMonths[jMonth - 1]
                    "n" -> result += jMonth.toString()
                    "t" -> result += if (jMonth != 12) {
                        (31 - (jMonth / 7).toInt())
                    } else {
                        kab + 29
                    }
                    
                    // Year
                    "L" -> result += kab.toString()
                    "Y" -> result += jYear.toString()
                    "y" -> result += jYear.toString().substring(2)
                    
                    // Time
                    "a" -> result += if (hour < 12) "ق.ظ" else "ب.ظ"
                    "A" -> result += if (hour < 12) "قبل از ظهر" else "بعد از ظهر"
                    "g" -> result += (if (hour == 0) 12 else if (hour > 12) hour - 12 else hour).toString()
                    "G" -> result += hour.toString()
                    "h" -> {
                        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        result += if (h < 10) "0$h" else h.toString()
                    }
                    "H" -> result += if (hour < 10) "0$hour" else hour.toString()
                    "i" -> result += if (minute < 10) "0$minute" else minute.toString()
                    "s" -> result += if (second < 10) "0$second" else second.toString()
                    
                    // Full date/time
                    "c" -> {
                        result += "$jYear/$jMonth/$jDay، $hour:$minute:$second"
                    }
                    "r" -> {
                        result += "$hour:$minute:$second ${persianDays[dayOfWeek]}، $jDay ${persianMonths[jMonth - 1]} $jYear"
                    }
                    
                    // Other
                    "U" -> result += timestamp.toString()
                    "f" -> result += seasons[(jMonth / 3.1).toInt()]
                    "b" -> result += ((jMonth / 3.1).toInt() + 1).toString()
                    "S" -> result += "ام"
                    
                    else -> result += sub
                }
                
                i++
            }
            
            return if (convertToFarsi) convertToFarsi(result) else result
        }
        
        /**
         * Convert a Gregorian date to Jalali (Persian) date
         * 
         * @param gy Gregorian year
         * @param gm Gregorian month
         * @param gd Gregorian day
         * @return Array of [year, month, day] in Jalali calendar
         */
        fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
            val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
            var jy: Int
            var year = gy
            var month = gm
            var day = gd
            
            if (year > 1600) {
                jy = 979
                year -= 1600
            } else {
                jy = 0
                year -= 621
            }
            
            val gy2 = if (month > 2) year + 1 else year
            var days = (365 * year) + 
                    ((gy2 + 3) / 4) - 
                    ((gy2 + 99) / 100) + 
                    ((gy2 + 399) / 400) - 
                    80 + day + gDaysInMonth[month - 1]
            
            jy += 33 * (days / 12053)
            days %= 12053
            
            jy += 4 * (days / 1461)
            days %= 1461
            
            jy += days / 365
            if (days > 365) days = (days - 1) % 365
            
            val jm: Int
            val jd: Int
            
            if (days < 186) {
                jm = 1 + (days / 31)
                jd = 1 + (days % 31)
            } else {
                jm = 7 + ((days - 186) / 30)
                jd = 1 + ((days - 186) % 30)
            }
            
            return intArrayOf(jy, jm, jd)
        }
        
        /**
         * Convert a Jalali (Persian) date to Gregorian date
         * 
         * @param jy Jalali year
         * @param jm Jalali month
         * @param jd Jalali day
         * @return Array of [year, month, day] in Gregorian calendar
         */
        fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): IntArray {
            var year = jy
            var month = jm
            var day = jd
            var gy: Int
            
            if (year > 979) {
                gy = 1600
                year -= 979
            } else {
                gy = 621
            }
            
            var days = (365 * year) + 
                    ((year / 33) * 8) + 
                    (((year % 33) + 3) / 4) + 
                    78 + day + 
                    (if (month < 7) (month - 1) * 31 else ((month - 7) * 30) + 186)
            
            gy += 400 * (days / 146097)
            days %= 146097
            
            if (days > 36524) {
                gy += 100 * (--days / 36524)
                days %= 36524
                if (days >= 365) days++
            }
            
            gy += 4 * (days / 1461)
            days %= 1461
            
            gy += days / 365
            if (days > 365) days = (days - 1) % 365
            
            var gd = days + 1
            val sal_a = intArrayOf(
                0, 31, 
                if ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0) 29 else 28, 
                31, 30, 31, 30, 31, 31, 30, 31, 30, 31
            )
            
            var gm = 0
            for (i in 0 until 13) {
                val v = sal_a[i]
                if (gd <= v) {
                    gm = i
                    break
                }
                gd -= v
            }
            
            return intArrayOf(gy, gm, gd)
        }
        
        /**
         * Check if a Jalali year is a leap year
         * 
         * @param year Jalali year
         * @return true if it's a leap year, false otherwise
         */
        fun isJalaliLeapYear(year: Int): Boolean {
            // Persian leap years follow a specific pattern in a 33-year cycle
            // Years 1, 5, 9, 13, 17, 21, 25, 29 in each 33-year cycle are leap years
            val yearInCycle = year % 33
            return yearInCycle == 1 || yearInCycle == 5 || yearInCycle == 9 || 
                   yearInCycle == 13 || yearInCycle == 17 || yearInCycle == 22 ||
                   yearInCycle == 26 || yearInCycle == 30
        }
        
        /**
         * Convert latin numbers to Persian numbers
         * 
         * @param str The string to convert
         * @return String with Persian numbers
         */
        fun convertToFarsi(str: String): String {
            var result = str
            for (i in 0 until 10) {
                result = result.replace(latinNumbers[i], persianNumbers[i])
            }
            return result
        }
        
        /**
         * Convert Persian numbers to latin numbers
         * 
         * @param str The string to convert
         * @return String with latin numbers
         */
        fun convertToLatin(str: String): String {
            var result = str
            for (i in 0 until 10) {
                result = result.replace(persianNumbers[i], latinNumbers[i])
            }
            return result
        }
        
        /**
         * Get current date formatted in Jalali calendar
         * 
         * @param format The desired format string
         * @param convertToFarsi Whether to convert numbers to Persian
         * @return Formatted current Jalali date string
         */
        fun now(format: String = "Y/m/d H:i:s", convertToFarsi: Boolean = true): String {
            return jdate(format, System.currentTimeMillis() / 1000, "Asia/Tehran", convertToFarsi)
        }
        
        /**
         * Get number of days in a Jalali month
         * 
         * @param year Jalali year
         * @param month Jalali month
         * @return Number of days in the month
         */
        fun daysInMonth(year: Int, month: Int): Int {
            return when {
                month <= 6 -> 31
                month <= 11 -> 30
                isJalaliLeapYear(year) -> 30
                else -> 29
            }
        }
        
        /**
         * Convert a Date object to a Jalali formatted date string
         * 
         * @param date The Date object to convert
         * @param format The desired format string
         * @param convertToFarsi Whether to convert numbers to Persian
         * @return Formatted Jalali date string
         */
        fun dateToJalali(date: Date, format: String = "Y/m/d", convertToFarsi: Boolean = true): String {
            return jdate(format, date.time / 1000, "Asia/Tehran", convertToFarsi)
        }
    }
} 