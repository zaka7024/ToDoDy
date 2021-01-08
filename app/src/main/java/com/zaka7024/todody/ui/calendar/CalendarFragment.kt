package com.zaka7024.todody.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.databinding.CalendarDayLayoutBinding
import com.zaka7024.todody.databinding.FragmentCalendarBinding
import com.zaka7024.todody.ui.home.TaskFragment
import com.zaka7024.todody.ui.home.showCalendar
import com.zaka7024.todody.ui.home.showCreateTodoDialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class CalendarFragment : Fragment(R.layout.fragment_calendar) {
    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarViewModel: CalendarViewModel

    class DayViewContainer(view: View, var setOnDayViewClickListener :SetOnDayViewClickListener? = null) : ViewContainer(view) {

        interface SetOnDayViewClickListener {
            fun onclick()
        }

        val binding = CalendarDayLayoutBinding.bind(view)
        val textView = binding.calendarDayText
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                setOnDayViewClickListener?.onclick()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        calendarViewModel = ViewModelProviders.of(this).get(CalendarViewModel::class.java)
        binding = FragmentCalendarBinding.bind(view)

        binding.addTask.setOnClickListener {
            showCreateTodoDialog(requireContext(), object : TaskFragment.CalendarEventsListener {
                override fun onSelectTime(calendar: Calendar) {

                }

                override fun onSelectReminder(calendar: Calendar) {
                }

                override fun onSelectDate(localDate: LocalDate) {
                }
            }, object : TaskFragment.TodoCreateListener {
                override fun onSend(todo: Todo) {
                    if (todo.title.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please enter some thing",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        //todos.add(todo)
                        //todayTodoAdapter.notifyItemInserted(todos.size - 1)
                    }
                }
            })
        }

        //
        setUpCalendar()
    }

    private fun setUpCalendar() {
        binding.apply {
            val todayMonth = YearMonth.now()
            val monthsToAdd = 12L
            val lastMonth = todayMonth.plusMonths(monthsToAdd)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

            calendarView.setup(todayMonth, lastMonth, firstDayOfWeek)
            calendarView.scrollToMonth(todayMonth)

            // Bind the view holder
            calendarView.dayBinder = object : DayBinder<DayViewContainer> {
                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    container.textView.text = day.date.dayOfMonth.toString()

                    container.day = day
                    container.setOnDayViewClickListener = object : DayViewContainer.SetOnDayViewClickListener {
                        override fun onclick() {
                            if (day.owner == DayOwner.THIS_MONTH) {
                                calendarViewModel.setCurrentSelectedDay(day.date)
                                calendarView.notifyCalendarChanged()
                            }
                        }
                    }

                    if (calendarViewModel.currentSelectedDay.value == day.date) {
                        container.textView.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.primaryDarkColor,
                                null
                            )
                        )
                        container.textView.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.calendar_day_bg,
                            null
                        )
                    }else {
                        container.textView.background = null
                    }

                    if (day.owner == DayOwner.THIS_MONTH) {
                        container.textView.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.primaryColor,
                                null
                            )
                        )
                    } else {
                        container.textView.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.secondaryDarkColor,
                                null
                            )
                        )
                    }
                }
                override fun create(view: View) = DayViewContainer(view)
            }
        }
    }
}
