package com.zaka7024.todody.ui.task

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zaka7024.todody.R

class ItemTouchHelperCallback(
    context: Context,
    val onSwiped: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    val icon =
        ContextCompat.getDrawable(
            context,
            R.drawable.ic_baseline_calendar_today_24
        )
    val background = ContextCompat.getDrawable(context, R.drawable.rounded)

    init {
        background?.setTintList(
            ContextCompat.getColorStateList(
                context,
                R.color.redColor
            )
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        this.onSwiped(position)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
        background?.setBounds(
            viewHolder.itemView.left,
            viewHolder.itemView.top,
            viewHolder.itemView.right,
            viewHolder.itemView.bottom
        )

        background?.draw(c)
        icon?.draw(c)
    }
}
