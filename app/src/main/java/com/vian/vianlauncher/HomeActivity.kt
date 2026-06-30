package com.vian.vianlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeActivity : ComponentActivity() {

    private val TAG = "HomeActivity"
    private lateinit var viewPager: ViewPager2
    private lateinit var llDock: LinearLayout
    private lateinit var flDrawer: FrameLayout
    private lateinit var rvDrawer: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var ivWallpaper: ImageView

    private val scope = CoroutineScope(Dispatchers.Main)
    private var allApps = listOf<AppItem>()
    private lateinit var drawerAdapter: AppGridAdapter

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            AppLogger.d(TAG, "Package changed: ${intent.action}")
            loadApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d(TAG, "onCreate")
        setContentView(R.layout.activity_home)

        viewPager = findViewById(R.id.view_pager)
        llDock = findViewById(R.id.ll_dock)
        flDrawer = findViewById(R.id.fl_drawer)
        rvDrawer = findViewById(R.id.rv_drawer)
        etSearch = findViewById(R.id.et_search)
        ivWallpaper = findViewById(R.id.iv_wallpaper)

        setupDrawer()
        loadWallpaper()
        setupDock()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (flDrawer.visibility == View.VISIBLE) {
                    closeDrawer()
                }
            }
        })

        loadApps()
    }

    override fun onResume() {
        super.onResume()
        AppLogger.d(TAG, "onResume")
        // Check grid dimensions if changed
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d(TAG, "onDestroy")
        unregisterReceiver(packageReceiver)
    }

    private fun loadWallpaper() {
        scope.launch(Dispatchers.IO) {
            val prefs = getSharedPreferences("vian_launcher_prefs", Context.MODE_PRIVATE)
            val path = prefs.getString("custom_wallpaper_path", null)
            var bmp = if (path != null && File(path).exists()) {
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeFile(path, options)
            } else null
            
            withContext(Dispatchers.Main) {
                if (bmp != null) {
                    ivWallpaper.setImageBitmap(bmp)
                } else {
                    ivWallpaper.setBackgroundColor(Color.DKGRAY)
                }
            }
        }
    }

    private fun setupDock() {
        llDock.removeAllViews()
        // 5 fixed slots at bottom
        for (i in 0 until 5) {
            val view = layoutInflater.inflate(R.layout.item_app_icon, llDock, false)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            view.layoutParams = params
            
            val tvLabel = view.findViewById<TextView>(R.id.tv_label)
            val ivIcon = view.findViewById<ImageView>(R.id.iv_icon)
            
            if (i == 2) {
                // Center dock button
                tvLabel.text = "Drawer"
                ivIcon.setImageResource(android.R.drawable.ic_menu_sort_by_size)
                view.setOnClickListener { openDrawer() }
            } else {
                tvLabel.text = "App"
            }
            llDock.addView(view)
        }
    }

    private fun setupDrawer() {
        val prefs = getSharedPreferences("vian_launcher_prefs", Context.MODE_PRIVATE)
        val cols = prefs.getInt("grid_columns", 4)
        
        rvDrawer.layoutManager = GridLayoutManager(this, cols)
        drawerAdapter = AppGridAdapter(this, emptyList()) { app ->
            openApp(app)
        }
        rvDrawer.adapter = drawerAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().lowercase()
                drawerAdapter.updateData(allApps.filter { it.label.lowercase().contains(q) })
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openDrawer() {
        flDrawer.visibility = View.VISIBLE
        etSearch.text.clear()
        drawerAdapter.updateData(allApps)
    }

    private fun closeDrawer() {
        flDrawer.visibility = View.INVISIBLE
    }

    private fun loadApps() {
        scope.launch(Dispatchers.IO) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val pm = packageManager
            val apps = pm.queryIntentActivities(mainIntent, 0).map { info ->
                AppItem(
                    info.activityInfo.packageName,
                    info.activityInfo.name,
                    info.loadLabel(pm).toString()
                )
            }.sortedBy { it.label }
            
            withContext(Dispatchers.Main) {
                allApps = apps
                if (flDrawer.visibility == View.VISIBLE) {
                    drawerAdapter.updateData(allApps)
                }
            }
        }
    }

    private fun openApp(app: AppItem) {
        AppLogger.d(TAG, "Opening app: ${app.packageName}")
        val intent = Intent().apply {
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
            closeDrawer()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to open app", e)
        }
    }
}
