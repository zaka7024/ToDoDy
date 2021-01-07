package com.zaka7024.todody.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.databinding.TodoItemBinding

class TodoAdapter(private val todos: MutableList<Todo>) :
    RecyclerView.Adapter<TodoAdapter.TodoHolder>() {

    class TodoHolder(
        private val todoItemBinding: TodoItemBinding,
        private val todos: MutableList<Todo>
    ) :
        RecyclerView.ViewHolder(
            todoItemBinding.root
        ) {

        fun bind(itemPosition: Int) {
            val todo = todos[itemPosition]
            todoItemBinding.todoText.text = todo.title

            todoItemBinding.root.animate().translationYBy(20f)

            if(todo.subItems.isNotEmpty()) {
                todoItemBinding.hint.visibility = View.VISIBLE
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
        holder.bind(position)
    }

    override fun getItemCount() = todos.size
}
