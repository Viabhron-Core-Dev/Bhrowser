package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity

class LogViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d("LogViewerActivity", "onCreate")
        // Implementation to be built
    }
}
