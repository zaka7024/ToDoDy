package com.zaka7024.todody.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodosWithSubitems
import com.zaka7024.todody.databinding.CalendarDayLayoutBinding
import com.zaka7024.todody.databinding.FragmentCalendarBinding
import com.zaka7024.todody.ui.task.TaskFragment
import com.zaka7024.todody.ui.task.TodoAdapter
import com.zaka7024.todody.ui.task.showCreateTodoDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@AndroidEntryPoint
class CalendarFragment : Fragment(R.layout.fragment_calendar) {
    private lateinit var binding: FragmentCalendarBinding
    private val calendarViewModel by viewModels<CalendarViewModel>()
    private lateinit var todoAdapter: TodoAdapter

    class DayContainer(
        view: View,
        var setOnDayViewClickListener: SetOnDayViewClickListener? = null
    ) : ViewContainer(view) {

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

        binding = FragmentCalendarBinding.bind(view)

        val todos = mutableListOf<TodosWithSubitems>()
        todoAdapter = TodoAdapter(todos, object : TodoAdapter.OnTodoHolderEventsListener {
            override fun onClick(todoItem: TodosWithSubitems) {
                findNavController().navigate(
                    CalendarFragmentDirections.actionCalendarFragmentToTodoEditor(todoItem)
                )
            }

            override fun onCompleteTodo(todoItem: TodosWithSubitems) {
                calendarViewModel.updateTodo(todoItem.todo)
            }
        })

        binding.apply {
            todosRv.adapter = todoAdapter
            todosRv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        calendarViewModel.categories.observe(viewLifecycleOwner, { userCategories ->
            // Add new t,odo to the database
            binding.addTask.setOnClickListener {
                showCreateTodoDialog(
                    requireContext(),
                    userCategories,
                    object : TaskFragment.TodoCreateListener {
                        override fun onSend(
                            todo: Todo,
                            subitems: List<Subitem>,
                            categoryNmae: String
                        ) {
                            if (todo.title.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "Please enter some thing",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                calendarViewModel.saveTodo(
                                    todo,
                                    subitems.toTypedArray(),
                                    categoryNmae
                                )
                                todoAdapter.notifyItemInserted(todos.size - 1)
                            }
                        }

                        override fun onCreateCategory(categoryName: String) {

                        }
                    }, calendarViewModel.currentSelectedDay.value
                )
            }
        })

        // Get all todos in the selected date
        calendarViewModel.userTodos.observe(viewLifecycleOwner) { userTodos ->
            todos.clear()
            todos.addAll(userTodos)
            todoAdapter.notifyDataSetChanged()

            binding.noTasksHint.isVisible = userTodos.isEmpty()
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
            calendarView.dayBinder = object : DayBinder<DayContainer> {
                override fun bind(container: DayContainer, day: CalendarDay) {
                    container.textView.text = day.date.dayOfMonth.toString()

                    container.day = day
                    container.setOnDayViewClickListener =
                        object : DayContainer.SetOnDayViewClickListener {
                            override fun onclick() {
                                if (day.owner == DayOwner.THIS_MONTH) {
                                    calendarViewModel.setCurrentSelectedDay(day.date)
                                    calendarView.notifyCalendarChanged()
                                }
                            }
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

                    if (calendarViewModel.currentSelectedDay.value == day.date) {
                        container.textView.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.primaryLightColor,
                                null
                            )
                        )
                        container.textView.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.calendar_day_bg,
                            null
                        )
                    } else {
                        container.textView.background = null
                    }
                }

                override fun create(view: View) = DayContainer(view)
            }
        }
    }
}
