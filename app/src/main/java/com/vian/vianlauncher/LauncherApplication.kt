package com.vian.vianlauncher

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.util.LruCache
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LauncherApplication : Application() {

    val iconCache = LruCache<String, Bitmap>(60)

    override fun onCreate() {
        super.onCreate()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(throwable)
        }
    }

    private fun handleCrash(throwable: Throwable) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val crashFile = File(downloadsDir, "vian_launcher_crash_$timestamp.txt")
            val latestCrashFile = File(downloadsDir, "vian_launcher_crash_latest.txt")
            
            val deviceInfo = """
                Crash Report
                Timestamp: $timestamp
                Device: ${Build.MANUFACTURER} ${Build.MODEL}
                Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                App Version: 1.0
                
                Stacktrace:
                ${throwable.stackTraceToString()}
            """.trimIndent()
            
            FileWriter(crashFile).use { it.write(deviceInfo) }
            FileWriter(latestCrashFile).use { it.write(deviceInfo) }
            
        } catch (e: Exception) {
            // Can't do much if crash logger crashes
        } finally {
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }
}
