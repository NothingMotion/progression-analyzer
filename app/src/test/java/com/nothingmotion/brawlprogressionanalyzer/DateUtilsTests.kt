package com.nothingmotion.brawlprogressionanalyzer

import com.nothingmotion.brawlprogressionanalyzer.util.DateUtils
import org.junit.Test
import java.util.Date
import org.junit.Assert.*
import java.util.Calendar

class DateUtilsTests {
    @Test
    fun currentDateFormatted(){
        val nowFormatted = DateUtils.nowFormatted()
        println("DateUtilsTests $nowFormatted")
    }

    @Test
    fun currentDate(){
        println(DateUtils.now())
    }

    @Test
    fun formatDate(){
        // 2 means month march
        println(DateUtils.toFormatted( Date(2025,2,4)))
    }


    @Test
    fun nowToPersian(){
        println(DateUtils.toPersianFormatted(DateUtils.now(),DateUtils.PERSIAN_FORMAT))
    }

    @Test
    fun persianToGregorian(){
        DateUtils.toGregorian(1404,1,8).forEach {
            print("$it ")
        }
    }


    @Test
    fun toJalali(){
        DateUtils.toJalali(2025,3,28).forEach {
            print(it)
        }
    }
    @Test
    fun dateToPersian(){
        println(DateUtils.toPersianFormatted(Date(2025,2,2)))
    }

    @Test
    fun testParseDateWithDefaultFormat() {
        // Create a date string in default format
        val dateStr = "2024-05-15 10:30:45"
        
        // Parse it
        val parsedDate = DateUtils.parseDate(dateStr)
        
        // Format it back and verify it matches
        val formattedBack = DateUtils.formatDate(parsedDate, DateUtils.DEFAULT_FORMAT)
        assertEquals(dateStr, formattedBack)
    }
    
    @Test
    fun testParseDateWithCustomFormat() {
        // Custom format
        val customFormat = "dd/MM/yyyy"
        val dateStr = "15/05/2024"
        
        // Parse with custom format
        val parsedDate = DateUtils.parseDate(dateStr, customFormat)
        
        // Format back with same custom format and verify
        val formattedBack = DateUtils.formatDate(parsedDate, customFormat)
        assertEquals(dateStr, formattedBack)
    }
    
    @Test
    fun testFormatDateWithCustomFormat() {
        // Create a calendar instance for a specific date
        val cal = Calendar.getInstance()
        cal.set(2024, 4, 15, 10, 30, 45) // May 15, 2024, 10:30:45
        val date = cal.time
        
        // Format with custom format
        val formatted = DateUtils.formatDate(date, "yyyy-MM-dd")
        assertEquals("2024-05-15", formatted)
    }
    
    @Test
    fun testJalaliLeapYear() {
        // Test known Jalali leap years

        assertTrue(DateUtils.isJalaliLeapYear(1395))
        assertTrue(DateUtils.isJalaliLeapYear(1399))
        assertTrue(DateUtils.isJalaliLeapYear(1403))
        assertTrue(DateUtils.isJalaliLeapYear(1408))

        // Test known non-leap years
        assertFalse(DateUtils.isJalaliLeapYear(1400))
        assertFalse(DateUtils.isJalaliLeapYear(1401))
    }
    
    @Test
    fun testJalaliGregorianConversion() {
        // Test Jalali to Gregorian conversion
        val gregorian = DateUtils.toGregorian(1403, 2, 15)
        assertEquals(2024, gregorian[0])
        assertEquals(5, gregorian[1]) // May
        assertEquals(4, gregorian[2])
        
        // Convert back to Jalali and verify original values
        val jalali = DateUtils.toJalali(gregorian[0], gregorian[1], gregorian[2])
        assertEquals(1403, jalali[0])
        assertEquals(2, jalali[1])
        assertEquals(15, jalali[2])
    }
    
    @Test
    fun testToPersianFormattedWithDifferentFormats() {
        // Create a specific date
        val cal = Calendar.getInstance()
        cal.set(2024, 4, 15) // May 15, 2024
        val date = cal.time
        
        // Test with default persian format
        val persianFormatted = DateUtils.toPersianFormatted(date)
        
        // Test with custom persian format
        val customPersianFormatted = DateUtils.toPersianFormatted(date, DateUtils.PERSIAN_FULL_FORMAT)
        
        // Verify the custom format is longer (includes time)
        assertTrue(customPersianFormatted.length > persianFormatted.length)
    }
    
    @Test
    fun testToPersianFormattedWithLatinNumbers() {
        // Create a specific date
        val cal = Calendar.getInstance()
        cal.set(2024, 4, 15) // May 15, 2024
        val date = cal.time
        
        // Get persian formatted with latin numbers
        val persianWithLatinNumbers = DateUtils.toPersianFormatted(date, convertToPersian = false)
        
        // Verify it contains latin digits (0-9) not persian digits
        assertTrue(persianWithLatinNumbers.any { it in '0'..'9' })
    }
}