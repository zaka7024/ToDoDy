package com.zaka7024.todody

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.databinding.TodoSubitemBinding


class TodoSublistAdapter(private val sublist: MutableList<Subitem>) :
    RecyclerView.Adapter<TodoSublistAdapter.SubitemHolder>() {

    var onSubitemEventsListener: OnSubitemEventsListener? = null

    interface OnSubitemEventsListener {
        fun onClickDelete(itemPosition: Int)
        fun onClickEnter()
        fun onTextChange(itemPosition: Int, text: String)
        fun onComplete(subitem: Subitem)
    }

    inner class SubitemHolder(private val todoSubitemBinding: TodoSubitemBinding) :
        RecyclerView.ViewHolder(
            todoSubitemBinding.root
        ) {

        fun bind() {
            val subitem = sublist[absoluteAdapterPosition]
            todoSubitemBinding.subItemEditText.setText(sublist[absoluteAdapterPosition].item)

            // Focus on EditText and show the keyboard
            if (todoSubitemBinding.subItemEditText.requestFocus()) {
                val inputMethodManager =
                    todoSubitemBinding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager!!.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }

            if (subitem.completed) {
                styleAsDone()
            }

            todoSubitemBinding.apply {
                subItemEditText.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        onSubitemEventsListener?.onClickEnter()
                    }
                    true
                }

                subItemEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        onSubitemEventsListener?.onTextChange(absoluteAdapterPosition, s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                })

                delete.setOnClickListener {
                    onSubitemEventsListener?.onClickDelete(absoluteAdapterPosition)
                }

                // Toggle done state
                circle.setOnClickListener {
                    subitem.completed = !subitem.completed
                    if(subitem.completed) {
                        styleAsDone()
                    } else {
                        removeDoneStyle()
                    }
                    onSubitemEventsListener?.onComplete(subitem)
                }
            }
        }

        private fun styleAsDone() {
            todoSubitemBinding.apply {
                circle.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        root.context.resources,
                        R.drawable.ic_baseline_check_circle_24, null
                    )
                )

                subItemEditText.paintFlags = subItemEditText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        private fun removeDoneStyle() {
            todoSubitemBinding.apply {
                circle.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        root.context.resources,
                        R.drawable.ic_round_radio_button_unchecked_24, null
                    )
                )

                subItemEditText.paintFlags = 0
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodoSublistAdapter.SubitemHolder {
        val binding = TodoSubitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubitemHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoSublistAdapter.SubitemHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = sublist.size
}