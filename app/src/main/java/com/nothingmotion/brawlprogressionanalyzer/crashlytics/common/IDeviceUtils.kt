package com.nothingmotion.brawlprogressionanalyzer.crashlytics.common


interface IDeviceUtils {
        fun getBatteryStatus(): String;
        fun getBatteryPercentage(): String
        fun getTotalStorage(): String;
        fun getFreeStorage(): String;
        fun getStoragePercentage(): Double;
        fun getTotalMemoryUsage(): String;
        fun getFreeMemoryUsage(): String;
        fun getMemoryPercentage(): Double;
        fun getNetworkInfo(): String;
        fun getAppVersion(): String;
        fun getDataSyncStatus(): String;
        fun getDeviceHealth(): String;
        fun getUserActivity(): String;
        fun getDeviceIdentity(): String;
        fun getWifiSignalStrength(): String
        fun getCellularSignalStrength(callback: (String) -> Unit);
        fun getNetworkType(): String;
        fun getUptime(): String;
//        fun startNetworkCallback(onNetworkChange: (String) -> Unit);
//        fun getDeviceId(): String

}