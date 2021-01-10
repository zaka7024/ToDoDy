package com.zaka7024.todody.ui.task

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodosWithSubitems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel @ViewModelInject constructor(private val todoRepository: TodoRepository)  : ViewModel() {

    private val _userTodos = todoRepository.userStoredTodos
    val userTodos: LiveData<List<TodosWithSubitems>>
        get() = _userTodos


    fun saveTodo(todo: Todo, list: Array<Subitem>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todoId = todoRepository.insertTodo(todo)
                list.forEach { it.todoOwnerId = todoId }
                todoRepository.insertSubitems(*list)
            }
        }
    }
}
