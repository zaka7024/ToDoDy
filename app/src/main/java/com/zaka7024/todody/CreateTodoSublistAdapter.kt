package com.zaka7024.todody

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.databinding.TodoSubitemBinding


class CreateTodoSublistAdapter(private val sublist: MutableList<Subitem>) :
    RecyclerView.Adapter<CreateTodoSublistAdapter.SubitemHolder>() {

    var onSubitemEventsListener: OnSubitemEventsListener? = null

    interface OnSubitemEventsListener {
        fun onClickDelete(itemPosition: Int)
        fun onClickEnter()
        fun onTextChange(itemPosition: Int, text: String)
    }

    inner class SubitemHolder(private val todoSubitemBinding: TodoSubitemBinding) :
        RecyclerView.ViewHolder(
            todoSubitemBinding.root
        ) {

        fun bind(itemPosition: Int) {
            todoSubitemBinding.subItemEditText.setText(sublist[itemPosition].item)

            // Focus on EditText and show the keyboard
            if (todoSubitemBinding.subItemEditText.requestFocus()) {
                val inputMethodManager =
                    todoSubitemBinding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager!!.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
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
                        onSubitemEventsListener?.onTextChange(itemPosition, s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                })

                delete.setOnClickListener {
                    onSubitemEventsListener?.onClickDelete(itemPosition)
                }
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