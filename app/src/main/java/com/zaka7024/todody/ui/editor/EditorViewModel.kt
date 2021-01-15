package com.zaka7024.todody.ui.editor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodosWithSubitems
import com.zaka7024.todody.ui.task.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class EditorViewModel @ViewModelInject constructor(private val todoRepository: TodoRepository) :
    ViewModel() {

    private val _categories = todoRepository.categories
    val categories: LiveData<List<Category>>
        get() = _categories

    private val _currentCategory = MutableLiveData<Category>()
    val currentCategory: LiveData<Category>
        get() = _currentCategory

    fun updateSubitem(subitem: Subitem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.updateSubitem(subitem)
            }
        }
    }

    fun removeTodo(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.removeTodo(todo)
            }
        }
    }

    fun getCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = withContext(Dispatchers.IO) {
                todoRepository.getCategoryById(categoryId)
            }
            _currentCategory.postValue(category)
        }
    }

    suspend fun addSubitem(subitem: Subitem): Long = runBlocking {
        todoRepository.insertSubitem(subitem)
    }

    fun removeSubitem(subitem: Subitem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.removeSubitem(subitem)
            }
        }
    }

    fun updateTodoCategory(todo: Todo, categoryName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val categoryId = todoRepository.getCategoryByName(categoryName).categoryId
                todo.categoryOwnerId = categoryId
                todoRepository.updateTodo(todo)
            }
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.updateTodo(todo)
            }
        }
    }
}