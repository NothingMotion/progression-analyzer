package com.nothingmotion.brawlprogressionanalyzer.crashlytics.common

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.google.gson.GsonBuilder
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import timber.log.Timber
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject


class CrashLytics constructor() {
    class ExceptionHandler @Inject constructor(
        @ApplicationContext private val context: Context,
        private val defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler,
        private val prefsManager: PreferencesManager
    ) : Thread.UncaughtExceptionHandler {
        var savingCrashJob: Job? = null
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            try {
                val deviceInfo = getDeviceInfo(context)


                val uuid = prefsManager.track?.uuid
                val crashReport = CrashReport(
                    uuid = uuid ?: UUID.randomUUID(),
                    timestamp = Date(System.currentTimeMillis()),
                    type = "Uncaught Exception",
                    throwable = throwable,
                    message = throwable.message,
                    stackTrace = throwable.stackTraceToString().toString(),
                    deviceInfo = deviceInfo

                )
                CoroutineScope(Dispatchers.IO).launch {
                    val filePath = saveCrashReport(crashReport)



                    Timber.tag("CrashLytics").d("Crash report saved at: $filePath")
                    val workRequest = getOneTimeRequestWorker(filePath)
                    val workManager = WorkManager.getInstance(context)

                    // This can remain on the IO thread
                    workManager.enqueueUniqueWork(
                        CRASHLYTICS_WORKER_TAG,
                        ExistingWorkPolicy.KEEP,
                        workRequest
                    )
                }
            } catch (e: Exception) {
                Timber.tag("CrashLytics").e("Error logging crash report: ${e.message}")
            } finally {
                // Call the default exception handler
                defaultUncaughtExceptionHandler.uncaughtException(thread, throwable)
            }
        }


        fun reportException(exception: Throwable) {

            this.uncaughtException(Thread.currentThread(), exception)
        }


        fun report(exception: Exception, type: String = "Normal Exception") {
            try {


                val deviceInfo = getDeviceInfo(context)
                val uuid = prefsManager.track?.uuid
                val crashReport = CrashReport(
                    uuid = uuid ?: UUID.randomUUID(),
                    timestamp = Date(System.currentTimeMillis()),
                    type = type,
                    throwable = exception,
                    message = exception.message,
                    stackTrace = exception.stackTraceToString().toString(),
                    deviceInfo = deviceInfo

                )
                CoroutineScope(Dispatchers.IO).launch {
                    val filePath = saveCrashReport(crashReport)



                    Timber.tag("CrashLytics").d("Crash report saved at: $filePath")
                    val workRequest = getOneTimeRequestWorker(filePath)
                    val workManager = WorkManager.getInstance(context)

                    // This can remain on the IO thread
                    workManager.enqueueUniqueWork(
                        CRASHLYTICS_WORKER_TAG,
                        ExistingWorkPolicy.KEEP,
                        workRequest
                    )
                }
            } catch (e: Exception) {
                Timber.tag("CrashLytics").e("Error logging crash report: ${e.message}")
            }
        }

        private fun getDeviceInfo(context: Context): DeviceInfo {
            val deviceUtils = DeviceUtils(context)
            val deviceInfo = DeviceInfo(
                appVersion = deviceUtils.getAppVersion(),
                deviceModel = Build.MODEL,
                deviceManufacturer = Build.MANUFACTURER,
                osVersion = Build.VERSION.RELEASE,
                batteryStatus = deviceUtils.getBatteryStatus(),
                batteryPercentage = deviceUtils.getBatteryPercentage(),
                totalStorage = deviceUtils.getTotalStorage(),
                freeStorage = deviceUtils.getFreeStorage(),
                storagePercentage = deviceUtils.getStoragePercentage(),
                totalMemoryUsage = deviceUtils.getTotalMemoryUsage(),
                freeMemoryUsage = deviceUtils.getFreeMemoryUsage(),
                memoryPercentage = deviceUtils.getMemoryPercentage(),
                networkInfo = deviceUtils.getNetworkInfo(),
                dataSyncStatus = deviceUtils.getDataSyncStatus(),
                healthStatus = deviceUtils.getDeviceHealth(),
                userActivity = deviceUtils.getUserActivity(),
                deviceIdentity = deviceUtils.getDeviceIdentity(),
                wifiSignalStrength = deviceUtils.getWifiSignalStrength(),
                cellularSignalStrength = null
            )
            return deviceInfo
        }

        private suspend fun saveCrashReport(crashReport: CrashReport): String {
            return try {
                withContext(Dispatchers.IO) {
                    val file = File(
                        context.cacheDir,
                        "crash_directory/crash_${crashReport.timestamp}.json"
                    )
                    // Create the parent directory if it doesn't exist
                    file.parentFile?.mkdirs()
                    file.writeText(GsonBuilder().create().toJson(crashReport))
                    return@withContext file.absolutePath
                }

            } catch (e: Exception) {
                Timber.tag("CrashLytics").e("Error saving crash report: ${e.message}")
                return ""
            }
        }

        companion object {
            lateinit var instance: ExceptionHandler
            private const val CRASHLYTICS_WORKER_TAG = "CrashLyticsWorker"
            const val CRASHLYTICS_CRASHREPORT_KEY = "crash_report"
            fun setup(context: Context, prefsManager: PreferencesManager) {
                val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                val exceptionHandler = ExceptionHandler(context, defaultHandler, prefsManager)
                Thread.setDefaultUncaughtExceptionHandler(exceptionHandler)
                if (!::instance.isInitialized) {
                    instance = ExceptionHandler(
                        context,
                        Thread.getDefaultUncaughtExceptionHandler(),
                        prefsManager
                    )
                }
            }

            fun getOneTimeRequestWorker(crashReportPath: String): OneTimeWorkRequest {

                return OneTimeWorkRequest.Builder(CrashLyticsWorker::class.java)
                    .setInitialDelay(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL, 15000,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
                    .setInputData(
                        workDataOf(
                            CRASHLYTICS_CRASHREPORT_KEY to crashReportPath
                        )
                    )
                    .setConstraints(
                        Constraints(
                            requiredNetworkType = NetworkType.CONNECTED,
                            requiresBatteryNotLow = true,
                        )
                    )
                    .build()
            }
        }
    }

    data class DeviceInfo(
        val appVersion: String = "",
        val deviceModel: String = "",
        val deviceManufacturer: String = "",
        val osVersion: String = "",
        val batteryStatus: String = "",
        val batteryPercentage: String = "",
        val totalStorage: String = "",
        val freeStorage: String = "",
        val storagePercentage: Double = 0.0,
        val totalMemoryUsage: String = "",
        val freeMemoryUsage: String = "",
        val memoryPercentage: Double = 0.0,
        val networkInfo: String = "",
        val dataSyncStatus: String = "",
        val healthStatus: String = "",
        val userActivity: String = "",
        val deviceIdentity: String = "",
        val wifiSignalStrength: String = "",
        val cellularSignalStrength: Int? = null,
    )

    data class CrashReport(
        val uuid: UUID,
        val timestamp: Date,
        val type: String,
        val throwable: Throwable?,
        val message: String?,
        val stackTrace: String?,
        val deviceInfo: DeviceInfo,
    )

    class DeviceUtils @Inject constructor(@ApplicationContext private val context: Context) :
        IDeviceUtils {


        var signalStrength: Int = 0;
        var signalStrengthPercentage: Int = 0;
        private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        private var networkCallback: ConnectivityManager.NetworkCallback? = null

        override fun getBatteryStatus(): String {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
                context.registerReceiver(null, it)
            }
            val deviceHealth = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val healthStatus = when (deviceHealth) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                else -> "Unknown"
            }
            return "Health: $healthStatus"
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getNetworkType(): String {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return "No connection";
            val netCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return "No connection";
            return when {
                netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
                netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                else -> "Other"
            }
        }

        override fun getBatteryPercentage(): String {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
                context.registerReceiver(null, it)
            }
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            return "$level%"
        }


        override fun getTotalStorage(): String {
            val storage = Environment.getExternalStorageDirectory()
            val totalStorage = storage.totalSpace / (1024 * 1024 * 1024)
            return "${totalStorage} GB Total"
        }

        override fun getFreeStorage(): String {
            val storage = Environment.getExternalStorageDirectory()
            val freeStorage = storage.freeSpace / (1024 * 1024 * 1024)
            return "${freeStorage} GB Available"
        }

        override fun getStoragePercentage(): Double {
            val storage = Environment.getExternalStorageDirectory()
            val totalStorage = storage.totalSpace.toDouble() / (1024 * 1024 * 1024)
            val freeStorage = storage.freeSpace.toDouble() / (1024 * 1024 * 1024)
            return freeStorage / totalStorage * 100;
        }

        override fun getTotalMemoryUsage(): String {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalMem = memoryInfo.totalMem / (1024 * 1024 * 1024)
            return "$totalMem GB"
        }

        override fun getFreeMemoryUsage(): String {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val usedMem = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024 * 1024)
            val totalMem = memoryInfo.totalMem / (1024 * 1024 * 1024)

            return "${totalMem - usedMem} GB"
        }

        override fun getMemoryPercentage(): Double {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalMem = memoryInfo.totalMem.toDouble()
            val availMem = memoryInfo.availMem.toDouble()
            val usedMem = totalMem - availMem
            return if (totalMem > 0) {
                (usedMem / totalMem) * 100
            } else {
                0.0
            }
        }

        override fun getNetworkInfo(): String {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return when {
                networkInfo?.isConnected == true && networkInfo.type == ConnectivityManager.TYPE_MOBILE -> "Connected to Cellular Mobile Data network"
                networkInfo?.isConnected == true && networkInfo.type == ConnectivityManager.TYPE_WIFI -> "Connected to Wi-Fi network"
                else -> "Offline"
            }
        }

        override fun getAppVersion(): String {
            val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            return "App Version: $version"
        }

        override fun getDataSyncStatus(): String {
            // TODO: Implementing this
            val lastSyncTime =
                "2024-11-04T12:00:00Z" // Example value, replace with actual sync time
            return "Last Sync: $lastSyncTime"
        }

        override fun getDeviceHealth(): String {
            val uptime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            return "Uptime: ${uptime / 1000 / 60} minutes"
        }

        override fun getUserActivity(): String {
            // TODO: Implementing this
            return "User logged in at 10:00 AM, last logout at 6:00 PM"
        }

        override fun getDeviceIdentity(): String {
            val buildNumber = Build.DISPLAY
            val androidRelease = Build.VERSION.RELEASE
            return "Android Version: $androidRelease, Build: $buildNumber"
        }

        override fun getWifiSignalStrength(): String {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return "Location permission not granted"
            }

            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo

            if (wifiManager.isWifiEnabled) {
                wifiInfo?.let {
                    if (it.networkId != -1) {
                        val rssi = it.rssi
                        val level = WifiManager.calculateSignalLevel(rssi, 5)
                        val percentage = (level * 100) / 4
                        return "$percentage%"
                    } else {
                        return "Not connected to Wi-Fi"
                    }
                }
            } else {
                return "Wi-Fi disabled"
            }
            return "N/A"
        }

        override fun getCellularSignalStrength(callback: (String) -> Unit) {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            telephonyManager.listen(object : PhoneStateListener() {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    super.onSignalStrengthsChanged(signalStrength)
                    // Update the signal strength
                    this@DeviceUtils.signalStrength = signalStrength.level
                    signalStrengthPercentage = signalStrength.level * 25
                }
            }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }


        override fun getUptime(): String {
            val uptime = SystemClock.elapsedRealtime()
            val days = uptime / (1000 * 60 * 60 * 24)
            val hours = (uptime / (1000 * 60 * 60)) % 24
            val minutes = (uptime / (1000 * 60)) % 60
            return String.format("Uptime: %d Days, %02d Hours, %02d Minutes", days, hours, minutes)
        }


//        override fun getDeviceId(): String {
//            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                telephonyManager.deviceId
//            } else {
//                telephonyManager.deviceId ?: "UNKNOWN_DEVICE_ID"
//            }
//        }


    }
}