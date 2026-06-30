package com.vian.vianlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkspacePagerAdapter(
    private val pages: Int,
    private val cols: Int,
    private val rows: Int,
    private val getItemsForPage: (Int) -> List<WorkspaceItem?>,
    private val onEmptyCellClick: (page: Int, col: Int, row: Int) -> Unit,
    private val onAppClick: (WorkspaceItem) -> Unit
) : RecyclerView.Adapter<WorkspacePagerAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvGrid: RecyclerView = view.findViewById(R.id.rv_grid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = position
        holder.rvGrid.layoutManager = object : GridLayoutManager(holder.itemView.context, cols) {
            override fun canScrollVertically() = false
        }
        
        holder.rvGrid.post {
            val cellHeight = holder.rvGrid.height / rows
            val items = getItemsForPage(page)
            holder.rvGrid.adapter = WorkspaceGridAdapter(cols, rows, cellHeight, items,
                onEmptyCellClick = { col, row -> onEmptyCellClick(page, col, row) },
                onAppClick = onAppClick
            )
        }
    }

    override fun getItemCount() = pages
}

class WorkspaceGridAdapter(
    private val cols: Int,
    private val rows: Int,
    private val cellHeight: Int,
    private val items: List<WorkspaceItem?>,
    private val onEmptyCellClick: (col: Int, row: Int) -> Unit,
    private val onAppClick: (WorkspaceItem) -> Unit
) : RecyclerView.Adapter<WorkspaceGridAdapter.CellViewHolder>() {

    class CellViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_icon, parent, false)
        view.layoutParams.height = cellHeight
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val col = position % cols
        val row = position / cols
        
        val item = items.find { it?.col == col && it?.row == row }
        // For simplicity, just handling UI logic loosely here.
        // Needs proper rendering for empty vs filled.
    }

    override fun getItemCount() = cols * rows
}
