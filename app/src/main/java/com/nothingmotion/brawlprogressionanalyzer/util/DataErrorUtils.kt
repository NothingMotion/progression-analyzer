package com.nothingmotion.brawlprogressionanalyzer.util

import android.database.sqlite.SQLiteAbortException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteException
import com.google.gson.JsonParseException
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

class DataErrorUtils @Inject constructor(
    private val crashlytics: CrashLytics.ExceptionHandler
) {

    companion object {
        @JvmStatic
        private lateinit var crashlyticsInstance: CrashLytics.ExceptionHandler

        fun initialize(crashlytics: CrashLytics.ExceptionHandler) {
            crashlyticsInstance = crashlytics
        }

        fun handleHttpException(e: Exception): DataError.NetworkError {
            crashlyticsInstance.report(e, "handleHttpException")
            when(e) {
                is SocketTimeoutException -> return DataError.NetworkError.TIMEOUT
                is InterruptedIOException -> return DataError.NetworkError.TIMEOUT
                is SSLHandshakeException -> return DataError.NetworkError.SSL_EXCEPTION
                is UnknownHostException -> return DataError.NetworkError.NO_INTERNET_CONNECTION
                is JsonParseException -> return DataError.NetworkError.UNPROCESSABLE_ENTITY
                is IOException -> return DataError.NetworkError.NO_INTERNET_CONNECTION
                is HttpException -> {
                    Timber.tag("AccountRepositoryImpl").e(e)
                    return when (e.code()) {
                        400 -> DataError.NetworkError.BAD_REQUEST
                        401 -> DataError.NetworkError.UNAUTHORIZED
                        403 -> DataError.NetworkError.FORBIDDEN
                        429 -> DataError.NetworkError.TOO_MANY_REQUESTS
                        500 -> DataError.NetworkError.SERVER_ERROR
                        else -> DataError.NetworkError.UNKNOWN
                    }
                }
                else -> return DataError.NetworkError.UNKNOWN
            }
        }

        fun handleDatabaseException(e: Exception, methodName: String): DataError.DatabaseError {
            Timber.tag("AccountRepositoryImpl").e("Database error in $methodName: ${e.message}")

            crashlyticsInstance.report(e, "handleDatabaseException")
            return when (e) {
                is SQLiteDiskIOException -> {
                    Timber.tag("AccountRepositoryImpl").e("Disk I/O error: ${e.message}")
                    DataError.DatabaseError.DISK_IO
                }
                is SQLiteAbortException -> {
                    Timber.tag("AccountRepositoryImpl").e("Transaction aborted: ${e.message}")
                    DataError.DatabaseError.ABORT_TRANSACTION
                }
                is SQLiteConstraintException -> {
                    if (e.message?.contains("UNIQUE constraint failed") == true) {
                        Timber.tag("AccountRepositoryImpl").e("Duplicate entry: ${e.message}")
                        DataError.DatabaseError.DUPLICATE_ENTRY
                    } else if (e.message?.contains("FOREIGN KEY constraint failed") == true) {
                        Timber.tag("AccountRepositoryImpl").e("Foreign key violation: ${e.message}")
                        DataError.DatabaseError.DATABASE_ERROR
                    } else {
                        Timber.tag("AccountRepositoryImpl").e("Constraint violation: ${e.message}")
                        DataError.DatabaseError.DATABASE_ERROR
                    }
                }
                is SQLiteException -> {
                    when {
                        e.message?.contains("no such table") == true -> {
                            Timber.tag("AccountRepositoryImpl").e("Table not found: ${e.message}")
                            DataError.DatabaseError.DATABASE_ERROR
                        }
                        e.message?.contains("no such column") == true -> {
                            Timber.tag("AccountRepositoryImpl").e("Column not found: ${e.message}")
                            DataError.DatabaseError.DATABASE_ERROR
                        }
                        else -> {
                            Timber.tag("AccountRepositoryImpl").e("SQLite error: ${e.message}")
                            DataError.DatabaseError.DATABASE_ERROR
                        }
                    }
                }
                else -> {
                    Timber.tag("AccountRepositoryImpl").e("Unknown database error: ${e.message}")
                    DataError.DatabaseError.UNKNOWN
                }
            }
        }
    }

    init {
        // Initialize the companion object with the injected crashlytics
        initialize(crashlytics)
    }
}