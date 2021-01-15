package com.zaka7024.todody.ui.editor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodosWithSubitems
import com.zaka7024.todody.ui.task.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel @ViewModelInject constructor(private val todoRepository: TodoRepository) :
    ViewModel() {

    fun updateSubitem(subitem: Subitem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.updateSubitem(subitem)
            }
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

    fun updateSubitems(todosWithSubitems: TodosWithSubitems) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoRepository.updateSubitems(todosWithSubitems)
            }
        }
    }
}