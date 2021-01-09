package com.zaka7024.todody.ui.task

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaka7024.todody.data.Todo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel @ViewModelInject constructor(private val todoRepository: TodoRepository)  : ViewModel() {

    private val _userTodos = todoRepository.userStoredTodos
    val userTodos: LiveData<List<Todo>>
        get() = _userTodos


    fun saveTodo(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.insertTodo(todo)
            }
        }
    }
}
