package com.zaka7024.todody.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.zaka7024.todody.R
import com.zaka7024.todody.data.TodosWithSubitems
import com.zaka7024.todody.databinding.TodoItemBinding

class TodoAdapter(
    private val todos: MutableList<TodosWithSubitems>,
    private val onTodoHolderEventsListener: OnTodoHolderEventsListener? = null
) :
    RecyclerView.Adapter<TodoAdapter.TodoHolder>() {

    interface OnTodoHolderEventsListener {
        fun onClick(todoItem: TodosWithSubitems)
        fun onCompleteTodo(todoItem: TodosWithSubitems)
    }

    inner class TodoHolder(
        private val todoItemBinding: TodoItemBinding,
        private val todos: MutableList<TodosWithSubitems>
    ) :
        RecyclerView.ViewHolder(
            todoItemBinding.root
        ) {

        fun bind() {
            val todoItem = todos[bindingAdapterPosition]
            todoItemBinding.apply {
                todoText.text = todoItem.todo.title
                date.text = todoItem.todo.date.toString()

                // Check if this t'odo completed
                if (todoItem.todo.completed) {
                    styleAsDone()
                }

                todoBg.setOnClickListener {
                    onTodoHolderEventsListener?.onClick(todoItem)
                }

                todoItemBinding.circle.setOnClickListener {
                    todoItem.todo.completed = !todoItem.todo.completed
                    if(todoItem.todo.completed) {
                        styleAsDone()
                    } else {
                        removeDoneStyle()
                    }
                    onTodoHolderEventsListener?.onCompleteTodo(todoItem)
                }
            }

            val offset = 20f
            todoItemBinding.root.translationY -= offset
            todoItemBinding.root.animate().translationYBy(offset)

            // Tod'd properties icon hints
            if (todoItem.subitems.isEmpty()) {
                todoItemBinding.subitemsHint.visibility = View.GONE
            }
            if (todoItem.todo.time == null) {
                todoItemBinding.timeHint.visibility = View.GONE
            }
            if (todoItem.todo.reminderTime == null) {
                todoItemBinding.alarmHint.visibility = View.GONE
            }
        }

        private fun styleAsDone() {
            todoItemBinding.apply {
                circle.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        root.context.resources,
                        R.drawable.ic_baseline_check_circle_24, null
                    )
                )
                todoText.paintFlags = todoText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        private fun removeDoneStyle() {
            todoItemBinding.apply {
                circle.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        root.context.resources,
                        R.drawable.ic_round_radio_button_unchecked_24, null
                    )
                )
                todoText.paintFlags = 0
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodoAdapter.TodoHolder {
        val binding = TodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoHolder(binding, todos)
    }

    override fun onBindViewHolder(holder: TodoHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = todos.size
}
