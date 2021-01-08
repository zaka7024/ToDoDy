package com.zaka7024.todody.ui.home

import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthScrollListener
import com.kizitonwose.calendarview.ui.ViewContainer
import com.zaka7024.todody.CreateTodoSublistAdapter
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.databinding.CalendarDayLayoutBinding
import com.zaka7024.todody.databinding.FragmentTaskBinding
import kotlinx.android.synthetic.main.todo_calendar_layout.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*


class TaskFragment : Fragment(R.layout.fragment_task) {

    private lateinit var todayTodoAdapter: TodoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)

        val todos = mutableListOf(Todo("Hello, World"))

        todayTodoAdapter = TodoAdapter(todos)

        binding.apply {
            todayRv.adapter = todayTodoAdapter
            todayRv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        binding.addTask.setOnClickListener {
            showCreateTodoDialog(requireContext(), object : CalendarEventsListener {
                override fun onSelectTime(calendar: Calendar) {

                }

                override fun onSelectReminder(calendar: Calendar) {
                }

                override fun onSelectDate(localDate: LocalDate) {
                }
            }, object : TodoCreateListener {
                override fun onSend(todo: Todo) {
                    if (todo.title.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please enter some thing",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        todos.add(todo)
                        todayTodoAdapter.notifyItemInserted(todos.size - 1)
                    }
                }
            })
        }
    }

    private fun showCreateTodoDialog(
        context: Context,
        calendarEventsListener: CalendarEventsListener,
        todoCreateListener: TodoCreateListener
    ) {

        val dialog = Dialog(context)
        dialog.apply {
            setContentView(R.layout.create_todo_layout)

            // Focus inTodo EditText and show th keyboard
            val todoEditText = findViewById<EditText>(R.id.todo_edit_text)
            todoEditText.requestFocus()
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )

            // Init sublist RecyclerView
            val sublistRecyclerView = findViewById<RecyclerView>(R.id.todo_sublist_rv)
            sublistRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val subTodoList = mutableListOf<String>()
            val adapter = CreateTodoSublistAdapter(subTodoList)

            adapter.onSubitemClickListener =
                object : CreateTodoSublistAdapter.OnSubitemClickListener {
                    override fun onClickDelete(itemPosition: Int) {
                        // Remove the subitem from the RecyclerView
                        subTodoList.removeAt(itemPosition)
                        adapter.notifyItemRemoved(itemPosition)
                        adapter.notifyItemRangeChanged(itemPosition, subTodoList.size)
                    }

                    // Function called when keyboard enter button clicked
                    override fun onClickEnter() {
                        subTodoList.add("Item ${subTodoList.size}")
                        adapter.notifyItemInserted(subTodoList.size - 1)
                        sublistRecyclerView.scrollToPosition(subTodoList.size - 1)
                    }
                }

            sublistRecyclerView.adapter = adapter

            // Send the todo
            val sendTodoButton = findViewById<ImageView>(R.id.todo_send)
            sendTodoButton.setOnClickListener {
                todoCreateListener.onSend(Todo(todoEditText.text.toString(), subTodoList))
                if (todoEditText.text.toString().isNotEmpty()) dismiss()
            }

            // Add the first subitem
            val todoListButton = findViewById<ImageView>(R.id.todo_list)
            todoListButton.setOnClickListener {
                subTodoList.add("Item ${subTodoList.size}")
                adapter.notifyItemInserted(subTodoList.size - 1)
                sublistRecyclerView.scrollToPosition(subTodoList.size - 1)
            }

            // Category menu
            val todoCategoryButton = findViewById<TextView>(R.id.todo_category)
            val categories = arrayOf("Work", "Home")
            todoCategoryButton.text = categories.first()
            todoCategoryButton.setOnClickListener {
                val popupMenu = PopupMenu(context, it)
                val menu = popupMenu.menu
                //TODO:: Add icon to the (New Category) Item
                popupMenu.inflate(R.menu.category_menu)
                for (category in categories) {
                    menu.add(category)
                }
                popupMenu.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.add_category_item) {

                    }else{
                        todoCategoryButton.text = item.title
                    }
                    true
                }
                popupMenu.show()
            }

            // Show calender view
            val todoCalender = findViewById<ImageView>(R.id.todo_calendar)
            todoCalender.setOnClickListener {
                showCalendar(context, calendarEventsListener)
            }

            // Show the dialog from the bottom
            val animation = AnimationUtils.loadAnimation(
                context,
                R.anim.slide_in_bottom
            )
            dialog.findViewById<ConstraintLayout>(R.id.todo_create_view).startAnimation(animation)

            // Change dialog layout properties
            val window: Window = window!!
            window.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val wlp = window.attributes
            wlp.gravity = Gravity.BOTTOM
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = wlp

            // Show the dialog
            show()
        }
    }

    interface TodoCreateListener {
        fun onSend(todo: Todo)
    }

    interface CalendarEventsListener {
        fun onSelectTime(calendar: Calendar)
        fun onSelectReminder(calendar: Calendar)
        fun onSelectDate(localDate: LocalDate)
    }

    enum class CalendarSteps {
        Day, ThreeDay, Week
    }

    fun Activity.hideKeyboard() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = this.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showCalendar(context: Context, calendarEventsListener: CalendarEventsListener) {

        val dialog = Dialog(context)
        dialog.apply {
            setContentView(R.layout.todo_calendar_layout)

            val window = window!!
            window.setLayout(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val today = LocalDate.now()
            val todayMonth = YearMonth.now()
            var currentSelectedDay = today
            var currentMonth = todayMonth
            val monthsToAdd = 12L
            val lastMonth = currentMonth.plusMonths(monthsToAdd)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

            val beforeMonthButton = findViewById<ImageView>(R.id.beforeMonth)
            val nextMonthButton = findViewById<ImageView>(R.id.nextMonth)
            val timeButton = findViewById<TextView>(R.id.time)
            val reminderButton = findViewById<TextView>(R.id.reminder)
            val todayButton = findViewById<TextView>(R.id.todayButton)
            val nextThreeButton = findViewById<TextView>(R.id.threeDaysButton)

            // Setup the calendar view
            val calendarView = findViewById<CalendarView>(R.id.calendarView)
            calendarView.setup(currentMonth, lastMonth, firstDayOfWeek)
            calendarView.scrollToMonth(currentMonth)

            val currentMonthTextView = findViewById<TextView>(R.id.currentMonth)
            // Set month name
            currentMonthTextView.text = currentMonth.month.name

            // Calendar view  holder
            class DayViewContainer(view: View) : ViewContainer(view) {
                val binding = CalendarDayLayoutBinding.bind(view)
                val textView = binding.calendarDayText
                lateinit var day: CalendarDay

                init {
                    view.setOnClickListener {
                        // Select the day
                        if (day.owner == DayOwner.THIS_MONTH) {
                            currentSelectedDay = day.date
                            calendarView.notifyCalendarChanged()
                        }
                    }
                }
            }

            // Bind the view holder
            calendarView.dayBinder = object : DayBinder<DayViewContainer> {

                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    val textView = container.textView
                    textView.text = day.date.dayOfMonth.toString()
                    container.day = day

                    when (currentSelectedDay) {
                        day.date -> {
                            textView.setTextColor(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.primaryDarkColor,
                                    null
                                )
                            )
                            textView.background = ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.calendar_day_bg,
                                null
                            )
                        }
                        else -> {
                            //textView.setTextColorRes(R.color.example_1_white)
                            textView.background = null
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
                }

                override fun create(view: View) = DayViewContainer(view)
            }

            // Dismiss the dialog
            val cancelButton = findViewById<TextView>(R.id.cancelButton)
            cancelButton.setOnClickListener {
                dismiss()
            }

            fun updateCurrentMonth(month: YearMonth) {
                currentMonth = month
                currentMonthTextView.text = currentMonth.month.name
                calendarView.scrollToMonth(currentMonth)
            }

            // back to previous month if exists
            beforeMonthButton.setOnClickListener {
                if (currentMonth > YearMonth.now()) {
                    updateCurrentMonth(currentMonth.minusMonths(1))
                }
            }

            // Move to next month if exists
            nextMonthButton.setOnClickListener {
                if (currentMonth < YearMonth.now().plusMonths(monthsToAdd)) {
                    updateCurrentMonth(currentMonth.plusMonths(1))
                }
            }

            val calendar = Calendar.getInstance()

            //
            timeButton.setOnClickListener {
                val timePickerDialog = TimePickerDialog(
                    context,
                    { view, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)

                        calendarEventsListener.onSelectTime(calendar)

                    }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true
                )
                timePickerDialog.show()
            }

            //
            reminderButton.setOnClickListener {
                val timePickerDialog = TimePickerDialog(
                    context,
                    { view, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)

                    }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true
                )
                timePickerDialog.show()
            }

            //
            calendarView.monthScrollListener = object : MonthScrollListener {
                override fun invoke(month: CalendarMonth) {
                    updateCurrentMonth(month.yearMonth)
                }
            }

            val calendarStepViews = arrayOf(todayButton, threeDaysButton)

            // Update calendar selected day and update the step views colors
            fun selectCalendarDay(stepView: View, calendarSteps: CalendarSteps) {
                for (view in calendarStepViews) {
                    if (view != stepView) {
                        view.backgroundTintList = ContextCompat.getColorStateList(
                            context,
                            R.color.secondaryDarkColor
                        )
                        view.setTextColor(
                            ContextCompat.getColorStateList(
                                context,
                                R.color.primaryTextColor
                            )
                        )
                    } else {
                        view.backgroundTintList = ContextCompat.getColorStateList(
                            context,
                            R.color.primaryDarkColor
                        )
                        view.setTextColor(
                            ContextCompat.getColorStateList(
                                context,
                                R.color.primaryLightColor
                            )
                        )
                    }
                }

                currentSelectedDay = when (calendarSteps) {
                    CalendarSteps.Day -> today
                    CalendarSteps.ThreeDay -> today.plusDays(3)
                    CalendarSteps.Week -> today.plusDays(7)
                }

                calendarView.scrollToMonth(todayMonth)
                calendarView.notifyCalendarChanged()
            }

            todayButton.setOnClickListener {
                selectCalendarDay(it, CalendarSteps.Day)
            }

            threeDaysButton.setOnClickListener {
                selectCalendarDay(it, CalendarSteps.ThreeDay)
            }

            show()
        }
    }
}
