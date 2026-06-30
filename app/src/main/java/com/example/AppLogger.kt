package com.example

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private const val MAX_LOG_SIZE = 1 * 1024 * 1024 // 1 MB

    private fun getLogFile(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDir, "vian_launcher_log.txt")
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        writeToFile("DEBUG", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val stackTrace = throwable?.stackTraceToString() ?: ""
        writeToFile("ERROR", tag, "$message\n$stackTrace")
    }

    private fun writeToFile(level: String, tag: String, message: String) {
        scope.launch {
            try {
                val file = getLogFile()
                if (file.exists() && file.length() > MAX_LOG_SIZE) {
                    truncateLogFile(file)
                }
                
                val timestamp = dateFormat.format(Date())
                val logLine = "$timestamp [$level] $tag: $message\n"
                
                FileWriter(file, true).use { writer ->
                    writer.append(logLine)
                }
            } catch (e: Exception) {
                Log.e("AppLogger", "Failed to write log to file", e)
            }
        }
    }

    private fun truncateLogFile(file: File) {
        try {
            val lines = file.readLines()
            val keepLines = lines.drop(lines.size / 2)
            FileWriter(file, false).use { writer ->
                keepLines.forEach { line ->
                    writer.append(line).append("\n")
                }
            }
        } catch (e: Exception) {
            Log.e("AppLogger", "Failed to truncate log file", e)
        }
    }
}
