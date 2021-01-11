package com.zaka7024.todody.ui.task

import androidx.lifecycle.LiveData
import com.zaka7024.todody.data.*
import javax.inject.Inject

class TodoRepository @Inject constructor(private val todoDao: TodoDao) {

    val categories = todoDao.getAllCategory()

    suspend fun getTodosWithinCategory(categoryName: String): CategoryWithTodos {
        return todoDao.getAllTodosWithinCategory(categoryName)
    }

    suspend fun insertTodo(todo: Todo): Long {
        return todoDao.insert(todo)
    }

    suspend fun insertCategory(category: Category): Long {
        return todoDao.insert(category)
    }

    suspend fun insertSubitems(vararg subitems: Subitem) {
        todoDao.insertSubitems(*subitems)
    }

    suspend fun getCategoryByName(categoryName: String): Category {
        return todoDao.getCategory(categoryName)
    }
}