package com.zaka7024.todody.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class CalendarViewModel : ViewModel() {
    private val _currentSelectedDay = MutableLiveData<LocalDate>()
    val currentSelectedDay : LiveData<LocalDate>
        get() = _currentSelectedDay

    fun setCurrentSelectedDay(localDate: LocalDate) {
        _currentSelectedDay.value = localDate
    }
}