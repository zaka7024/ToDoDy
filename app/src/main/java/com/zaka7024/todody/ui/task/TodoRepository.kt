package com.zaka7024.todody.ui.task

import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodoDao
import javax.inject.Inject

class TodoRepository @Inject constructor(private val todoDao: TodoDao) {

    val userStoredTodos = todoDao.getTodosWithSubitems()

    suspend fun insertTodo(todo: Todo): Long {
        return todoDao.insert(todo)
    }

    suspend fun insertSubitems(vararg subitems: Subitem) {
        todoDao.insertSubitems(*subitems)
    }
}