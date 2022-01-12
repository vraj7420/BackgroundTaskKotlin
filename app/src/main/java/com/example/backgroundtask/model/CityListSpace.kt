package com.example.backgroundtask.model

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CityListSpace(private var space: Int) : RecyclerView.ItemDecoration() {


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
       outRect.top=space
    }
}
