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

    fun getCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = withContext(Dispatchers.IO) {
                todoRepository.getCategoryById(categoryId)
            }
            _currentCategory.postValue(category)
        }
    }

    fun addSubitem(subitem: Subitem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.insertSubitems(subitem)
            }
        }
    }

    fun removeSubitem(subitem: Subitem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.removeSubitem(subitem)
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