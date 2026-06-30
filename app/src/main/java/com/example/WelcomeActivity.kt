package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import androidx.activity.ComponentActivity

class WelcomeActivity : ComponentActivity() {
    private val TAG = "WelcomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d(TAG, "onCreate")
        ModuleRegistry.init(this)

        val prefs = getSharedPreferences("vian_launcher_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("setup_complete", false)) {
            startHomeAndService()
            return
        }

        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.btn_grant_permissions).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } else {
                    completeSetup()
                }
            } else {
                completeSetup()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppLogger.d(TAG, "onResume")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                completeSetup()
            }
        }
    }

    private fun completeSetup() {
        val prefs = getSharedPreferences("vian_launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("setup_complete", true).apply()
        startHomeAndService()
    }

    private fun startHomeAndService() {
        val serviceIntent = Intent(this, LauncherService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
