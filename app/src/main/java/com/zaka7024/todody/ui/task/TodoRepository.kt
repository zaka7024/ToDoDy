package com.zaka7024.todody.ui.task

import androidx.lifecycle.LiveData
import com.zaka7024.todody.data.*
import java.time.LocalDate
import javax.inject.Inject

class TodoRepository @Inject constructor(private val todoDao: TodoDao) {

    val categories = todoDao.getAllCategory()
    val completedTodos = todoDao.getAllTodosCount(true)
    val pendingTodos = todoDao.getAllTodosCount(false)

    suspend fun getTodosWithinCategory(categoryName: String): CategoryWithTodos {
        return todoDao.getAllTodosWithinCategory(categoryName)
    }

    suspend fun getTodosWithinCategoryAndDate(localDate: LocalDate, categoryId: Long): List<TodosWithSubitems> {
        return todoDao.getAllTodosWithinDateCategory(localDate, categoryId)
    }

    suspend fun getTodosExceptDateWithinCategory(localDate: LocalDate, categoryId: Long): List<TodosWithSubitems> {
        return todoDao.getAllTodosExceptDateWithinCategory(localDate, categoryId)
    }

    suspend fun getAllTodosWithinDate(localDate: LocalDate): List<TodosWithSubitems> {
        return todoDao.getAllTodosWithinDate(localDate)
    }

    suspend fun getTodosCategory(): CategoryWithTodos {
        return todoDao.getAllTodosCategory()
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

    suspend fun insertSubitem(subitems: Subitem): Long {
        return todoDao.insertSubitem(subitems)
    }

    suspend fun getCategoryByName(categoryName: String): Category {
        return todoDao.getCategory(categoryName)
    }

    suspend fun getCategoryById(categoryId: Long): Category {
        return todoDao.getCategory(categoryId)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.update(todo)
    }

    suspend fun updateSubitems(todosWithSubitems: TodosWithSubitems) {
        todoDao.update(todosWithSubitems.subitems)
    }

    suspend fun updateSubitem(subitem: Subitem) {
        todoDao.update(subitem)
    }

    suspend fun removeSubitem(subitem: Subitem) {
        todoDao.removeSubitem(subitem)
    }

    suspend fun removeTodo(todo: Todo) {
        todoDao.removeTodo(todo)
    }
}