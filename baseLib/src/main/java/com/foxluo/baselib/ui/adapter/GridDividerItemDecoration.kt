package com.foxluo.baselib.ui.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridDividerItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val layoutManager = parent.layoutManager as GridLayoutManager
        val spanCount = layoutManager.spanCount
        val spanSizeLookup = layoutManager.spanSizeLookup

        val spanSize = spanSizeLookup.getSpanSize(position)
        val isTitle = spanSize == spanCount

        if (isTitle) {
            handleTitleItem(outRect, position)
        } else {
            handleGridItem(outRect, position, spanCount, spanSizeLookup)
        }
    }

    private fun handleTitleItem(outRect: Rect, position: Int) {
        outRect.left = 0
        outRect.right = 0
    }

    private fun handleGridItem(
        outRect: Rect,
        position: Int,
        spanCount: Int,
        spanSizeLookup: GridLayoutManager.SpanSizeLookup
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.top = spacing
    }

}