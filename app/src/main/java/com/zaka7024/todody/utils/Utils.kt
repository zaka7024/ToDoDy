package com.zaka7024.todody.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WrapContentLinearLayoutManager(context: Context, orientation: Int, reverse: Boolean) :
    LinearLayoutManager(context, orientation, reverse) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            //
        }
    }
}

class Utils {
    companion object {
        fun getDateTimeIn24Format(date: Date): String {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateFormat.format(date)
        }
    }
}