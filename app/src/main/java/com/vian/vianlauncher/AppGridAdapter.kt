package com.vian.vianlauncher

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppItem(val packageName: String, val activityName: String, val label: String)

class AppGridAdapter(
    private val context: Context,
    private var items: List<AppItem>,
    private val onItemClick: (AppItem) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.AppViewHolder>() {

    private val packageManager = context.packageManager
    private val application = context.applicationContext as LauncherApplication
    private val scope = CoroutineScope(Dispatchers.Main)

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        val tvLabel: TextView = view.findViewById(R.id.tv_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app_icon, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = items[position]
        holder.tvLabel.text = app.label
        holder.ivIcon.setImageDrawable(null) // clear

        val cacheKey = "${app.packageName}/${app.activityName}"
        val cachedIcon = application.iconCache.get(cacheKey)
        
        if (cachedIcon != null) {
            holder.ivIcon.setImageBitmap(cachedIcon)
        } else {
            // Load in background
            scope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    try {
                        val drawable = packageManager.getActivityIcon(
                            android.content.ComponentName(app.packageName, app.activityName)
                        )
                        drawableToBitmap(drawable)
                    } catch (e: Exception) {
                        null
                    }
                }
                bmp?.let {
                    application.iconCache.put(cacheKey, it)
                    if (holder.adapterPosition == position) {
                        holder.ivIcon.setImageBitmap(it)
                    }
                }
            }
        }

        holder.itemView.setOnClickListener { onItemClick(app) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<AppItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 144
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 144
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
