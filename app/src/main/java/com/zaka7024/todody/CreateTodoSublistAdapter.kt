package com.zaka7024.todody

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.zaka7024.todody.databinding.TodoSubitemBinding


class CreateTodoSublistAdapter(private val sublist: MutableList<String>) :
    RecyclerView.Adapter<CreateTodoSublistAdapter.SubitemHolder>() {

    var onSubitemClickListener: OnSubitemClickListener? = null

    interface OnSubitemClickListener {
        fun onClickDelete(itemPosition: Int)
        fun onClickEnter()
    }

    inner class SubitemHolder(private val todoSubitemBinding: TodoSubitemBinding) :
        RecyclerView.ViewHolder(
            todoSubitemBinding.root
        ) {

        fun bind(itemPosition: Int) {
            todoSubitemBinding.subItemEditText.setText(sublist[itemPosition])

            // Focus on EditText and show the keyboard
            if (todoSubitemBinding.subItemEditText.requestFocus()) {
                val inputMethodManager =
                    todoSubitemBinding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager!!.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }

            todoSubitemBinding.subItemEditText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    onSubitemClickListener?.onClickEnter()
                }
                true
            }

            todoSubitemBinding.delete.setOnClickListener {
                onSubitemClickListener?.onClickDelete(itemPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CreateTodoSublistAdapter.SubitemHolder {
        val binding = TodoSubitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubitemHolder(binding)
    }

    override fun onBindViewHolder(holder: CreateTodoSublistAdapter.SubitemHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = sublist.size
}