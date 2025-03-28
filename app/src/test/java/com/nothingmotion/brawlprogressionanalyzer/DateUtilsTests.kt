package com.nothingmotion.brawlprogressionanalyzer

import android.os.Debug
import android.util.Log
import com.nothingmotion.brawlprogressionanalyzer.util.DateUtils
import org.junit.Test
import java.util.Date

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

}