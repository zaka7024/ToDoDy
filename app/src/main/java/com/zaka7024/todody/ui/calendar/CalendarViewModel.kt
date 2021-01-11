package com.zaka7024.todody.ui.calendar

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaka7024.todody.data.CategoryWithTodos
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodosWithSubitems
import com.zaka7024.todody.ui.task.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CalendarViewModel @ViewModelInject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _currentSelectedDay = MutableLiveData<LocalDate>()
    val currentSelectedDay: LiveData<LocalDate>
        get() = _currentSelectedDay

    private var _userTodos = MutableLiveData<List<TodosWithSubitems>>()
    val userTodos: LiveData<List<TodosWithSubitems>>
        get() = _userTodos

    init {
        _currentSelectedDay.value = LocalDate.now()

        getTodos()
    }

    fun setCurrentSelectedDay(localDate: LocalDate) {
        _currentSelectedDay.value = localDate
        // get the todos for the new selected date
        getTodos()
    }

    private fun getTodos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todos = todoRepository.getAllTodosWithinDate(_currentSelectedDay.value!!)
                Log.i("taskViewModel", "todoRepository: $todos")
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
                getTodos()
            }
        }
    }
}