package com.nothingmotion.brawlprogressionanalyzer.util

import android.content.Context
import timber.log.Timber
import java.lang.Thread.UncaughtExceptionHandler

class GlobalExceptionHandling private constructor(private val defaultExceptionHandler: UncaughtExceptionHandler,private val context: Context): Thread.UncaughtExceptionHandler{
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        try {
            Timber.tag("GlobalExceptionHandling").e("Uncaught exception: ${p1.message}")

        }

        catch(e: Exception){
            Timber.tag("GlobalExceptionHandling").e("Error logging uncaught exception: ${e.message}")
        }
        finally {
            // Call the default exception handler
            defaultExceptionHandler.uncaughtException(p0, p1)
        }
    }
    companion object {
        fun setup(context: Context){
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            val globalExceptionHandler = GlobalExceptionHandling(defaultHandler,context)
            Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler)
        }
    }
}