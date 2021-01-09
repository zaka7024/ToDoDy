package com.zaka7024.todody.ui.task

import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodoDao
import javax.inject.Inject

class TodoRepository @Inject constructor(private val todoDao: TodoDao) {

    val userStoredTodos = todoDao.getAllTodos()

    suspend fun insertTodo(todo: Todo) {
        todoDao.insert(todo)
    }
}