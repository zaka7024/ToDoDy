package com.zaka7024.todody.ui.task

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.zaka7024.todody.data.*
import kotlinx.coroutines.*
import java.time.LocalDate

class TaskViewModel @ViewModelInject constructor(private val todoRepository: TodoRepository) :
    ViewModel() {

    private var _todayTodos = MutableLiveData<List<TodosWithSubitems>>()
    val todayTodos: LiveData<List<TodosWithSubitems>>
        get() = _todayTodos

    private var _otherTodos = MutableLiveData<List<TodosWithSubitems>>()
    val otherTodos: LiveData<List<TodosWithSubitems>>
        get() = _otherTodos

    private var _categories = todoRepository.categories
    val categories: LiveData<List<Category>>
        get() = _categories

    private var _currentSelectedCategory = MutableLiveData<Category>()
    val currentSelectedCategory: LiveData<Category>
        get() = _currentSelectedCategory

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.i("taskViewModel", "Init: insertCategory")
                todoRepository.insertCategory(Category(categoryName = "Home"))
                todoRepository.insertCategory(Category(categoryName = "Work"))
            }
        }

        getTodayTodos(1)
        getOthersTodos(1)
    }

    fun getTodayTodos(categoryId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todos = todoRepository.getTodosWithinCategoryAndDate(
                    LocalDate.now(), categoryId
                )
                Log.i("taskViewModel", "todoRepository: $todos")
                _todayTodos.postValue(todos)
            }
        }
    }

    fun getOthersTodos(categoryId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todos = todoRepository.getTodosExceptDateWithinCategory(
                    LocalDate.now(), categoryId
                )
                Log.i("taskViewModel", "todoRepository: $todos")
                _otherTodos.postValue(todos)
            }
        }
    }

    fun saveTodo(todo: Todo, list: Array<Subitem>, categoryName: String) {
        viewModelScope.launch {
            if (todo.date == null) {
                todo.date = LocalDate.now()
            }
            withContext(Dispatchers.IO) {
                // get the t.odo category
                val category = todoRepository.getCategoryByName(
                    categoryName
                )
                todo.categoryOwnerId = category.categoryId
                // links the subitem with the inserted T,odo
                val todoId = todoRepository.insertTodo(todo)
                list.forEach { it.todoOwnerId = todoId }
                todoRepository.insertSubitems(*list)

                // Update the database to trigger the observer
                getTodayTodos(currentSelectedCategory.value?.categoryId ?: 1)
                getOthersTodos(currentSelectedCategory.value?.categoryId ?: 1)
            }
        }
    }

    fun setCurrentCategory(category: Category) {
        _currentSelectedCategory.value = category
        // Update the todos
        getTodayTodos(category.categoryId ?: 1)
        getOthersTodos(category.categoryId ?: 1)
    }

    fun saveCategory(categoryName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.insertCategory(Category(categoryName = categoryName))
            }
        }
    }

    companion object {
        fun getAllCategories(context: Context): List<Category> {
            return Room.databaseBuilder(context, TodoDatabase::class.java,
                "todos_database")
                .build().daysDao().getAllCategories()
        }
    }
}
