package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

class LauncherService : Service() {
    private val CHANNEL_ID = "LauncherServiceChannel"
    private val TAG = "LauncherService"

    override fun onCreate() {
        super.onCreate()
        AppLogger.d(TAG, "onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.d(TAG, "onStartCommand")
        
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, settingsIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Vian Launcher")
                .setContentText("Vian Launcher running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Vian Launcher")
                .setContentText("Vian Launcher running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build()
        }

        startForeground(1, notification)
        
        // SIDEBAR_HOOK: Phase 3 — LauncherService will attach sidebar overlay here
        // SidebarManager.attach(windowManager) when sidebarEnabled = true
        
        return START_STICKY
    }

    override fun onDestroy() {
        AppLogger.d(TAG, "onDestroy")
        super.onDestroy()
        // Restart itself
        val broadcastIntent = Intent(this, BootReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Launcher Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
