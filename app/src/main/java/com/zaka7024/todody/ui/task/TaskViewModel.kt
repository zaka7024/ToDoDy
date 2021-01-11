package com.zaka7024.todody.ui.task

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.data.CategoryWithTodos
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel @ViewModelInject constructor(private val todoRepository: TodoRepository) :
    ViewModel() {

    private var _userTodos = MutableLiveData<CategoryWithTodos>()
    val userTodos: LiveData<CategoryWithTodos>
        get() = _userTodos

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

        getTodos(_currentSelectedCategory.value?.categoryName ?: "Home")
    }

    fun getTodos(categoryName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todos = todoRepository.getTodosWithinCategory(categoryName)
                Log.i("taskViewModel", "todoRepository: ${todos}")
                _userTodos.postValue(todos)
            }
        }
    }

    fun saveTodo(todo: Todo, list: Array<Subitem>, categoryName: String) {
        viewModelScope.launch {
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
                getTodos(currentSelectedCategory.value?.categoryName ?: "Home")
            }
        }
    }

    fun setCurrentCategory(category: Category) {
        _currentSelectedCategory.value = category
    }
}
