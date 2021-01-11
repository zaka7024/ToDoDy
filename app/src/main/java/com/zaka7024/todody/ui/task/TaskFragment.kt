package com.zaka7024.todody.ui.task

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodosWithSubitems
import com.zaka7024.todody.databinding.CalendarDayLayoutBinding
import com.zaka7024.todody.databinding.FragmentTaskBinding
import com.zaka7024.todody.ui.calendar.CalendarFragment
import com.zaka7024.todody.utils.WrapContentLinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.todo_calendar_layout.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task) {
    private lateinit var todayTodoAdapter: TodoAdapter
    private lateinit var othersTodoAdapter: TodoAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private val taskViewModel by viewModels<TaskViewModel>()

    // Calendar view  holder
    class DayViewContainer(
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
                // Select the day
                if (day.owner == DayOwner.THIS_MONTH) {
                    setOnDayViewClickListener?.onclick()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)

        val todos = mutableListOf<TodosWithSubitems>()
        val categories = mutableListOf<Category>()

        val otherTodos = mutableListOf(
            Todo(title = "Hello, Other World"),
            Todo(title = "Hello, Second World")
        )

        todayTodoAdapter = TodoAdapter(todos, object : TodoAdapter.OnTodoHolderClickListener {
            override fun onClick(todoItem: TodosWithSubitems) {
                findNavController().navigate(
                    TaskFragmentDirections.actionTaskFragmentToTodoEditor2(
                        todoItem
                    )
                )
            }
        })

        othersTodoAdapter = TodoAdapter(todos)

        //
        categoryAdapter =
            CategoryAdapter(categories, object : CategoryAdapter.SetOnCategoryClickListener {
                override fun onClick(category: Category) {
                    taskViewModel.setCurrentCategory(category)
                    taskViewModel.getTodos(category.categoryName)
                    binding.categoryRv.adapter = categoryAdapter
                }
            })

        binding.apply {
            todayRv.adapter = todayTodoAdapter
            todayRv.layoutManager =
                WrapContentLinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            otherRv.adapter = othersTodoAdapter
            otherRv.layoutManager =
                WrapContentLinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            categoryRv.adapter = categoryAdapter
            categoryRv.layoutManager =
                WrapContentLinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }

        // Get all todos form the selected category and notify the adapter
        taskViewModel.userTodos.observe(viewLifecycleOwner, { userTodos ->
            Log.i("taskViewModel", "todos: $userTodos")
            if (userTodos != null) {
                todos.clear()
                todos.addAll(userTodos.todos)
                todayTodoAdapter.notifyDataSetChanged()
            }
        })

        //
        taskViewModel.categories.observe(viewLifecycleOwner, { userCategories ->
            if (userCategories != null) {
                Log.i("taskViewModel", "userCategories: ${userCategories.size}")
                categoryAdapter.notifyItemRangeRemoved(0, categories.size)
                categories.clear()
                categories.addAll(userCategories)
                categoryAdapter.notifyDataSetChanged()
            }
        })


        binding.addTask.setOnClickListener {
            showCreateTodoDialog(requireContext(), object : TodoCreateListener {
                override fun onSend(todo: Todo, subitems: List<Subitem>, categoryName: String) {
                    if (todo.title.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please enter some thing",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        taskViewModel.saveTodo(todo, subitems.toTypedArray(), categoryName)
                        todayTodoAdapter.notifyItemInserted(todos.size - 1)
                    }
                }
            })
        }
    }

    interface TodoCreateListener {
        fun onSend(todo: Todo, subitems: List<Subitem>, categoryName: String)
    }

    interface CalendarEventsListener {
        fun onSelectTime(calendar: Calendar)
        fun onSelectReminder(calendar: Calendar)
        fun onSelectDate(localDate: LocalDate)
    }

    enum class CalendarSteps {
        Day, ThreeDay, Week
    }
}

fun showCreateTodoDialog(
    context: Context,
    todoCreateListener: TaskFragment.TodoCreateListener
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

        val subTodoList = mutableListOf<Subitem>()
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
                    subTodoList.add(Subitem(item = "Item ${subTodoList.size}"))
                    adapter.notifyItemInserted(subTodoList.size - 1)
                    sublistRecyclerView.scrollToPosition(subTodoList.size - 1)
                }
            }

        sublistRecyclerView.adapter = adapter

        // Add the first subitem
        val todoListButton = findViewById<ImageView>(R.id.todo_list)
        todoListButton.setOnClickListener {
            subTodoList.add(Subitem(item = "Item ${subTodoList.size}"))
            adapter.notifyItemInserted(subTodoList.size - 1)
            sublistRecyclerView.scrollToPosition(subTodoList.size - 1)
        }

        // Category menu
        val todoCategoryButton = findViewById<TextView>(R.id.todo_category)
        val categories = arrayOf("Home", "Work")
        var selectedCategory = categories.first()
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

                } else {
                    todoCategoryButton.alpha = 0f
                    todoCategoryButton.animate().alpha(1f).duration = 400
                    todoCategoryButton.text = item.title
                    selectedCategory = todoCategoryButton.text.toString()
                }
                true
            }
            popupMenu.show()
        }

        var time: Calendar? = null
        var reminderTime: Calendar? = null
        var selectedDate: LocalDate? = null

        // Show calender view
        val todoCalender = findViewById<ImageView>(R.id.todo_calendar)
        todoCalender.setOnClickListener {
            showCalendar(context, object : TaskFragment.CalendarEventsListener {
                override fun onSelectTime(calendar: Calendar) {
                    time = calendar
                }

                override fun onSelectReminder(calendar: Calendar) {
                    reminderTime = calendar
                }

                override fun onSelectDate(localDate: LocalDate) {
                    selectedDate = localDate
                }
            })
        }

        // Send the todo
        val sendTodoButton = findViewById<ImageView>(R.id.todo_send)
        sendTodoButton.setOnClickListener {
            todoCreateListener.onSend(
                Todo(
                    title = todoEditText.text.toString(),
                    time = time?.time,
                    reminderTime = reminderTime?.time,
                    date = selectedDate
                ),
                subitems = subTodoList,
                categoryName = selectedCategory
            )
            if (todoEditText.text.toString().isNotEmpty()) dismiss()
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

fun showCalendar(context: Context, calendarEventsListener: TaskFragment.CalendarEventsListener) {

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

        //
        calendarEventsListener.onSelectDate(currentSelectedDay)

        // Setup the calendar view
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setup(currentMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        val currentMonthTextView = findViewById<TextView>(R.id.currentMonth)
        // Set month name
        currentMonthTextView.text = currentMonth.month.name

        // Bind the view holder
        calendarView.dayBinder = object : DayBinder<TaskFragment.DayViewContainer> {

            override fun bind(container: TaskFragment.DayViewContainer, day: CalendarDay) {
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                container.day = day

                container.setOnDayViewClickListener = object :
                    TaskFragment.DayViewContainer.SetOnDayViewClickListener {
                    override fun onclick() {
                        currentSelectedDay = day.date
                        calendarEventsListener.onSelectDate(currentSelectedDay)
                        calendarView.notifyCalendarChanged()
                    }
                }

                when (currentSelectedDay) {
                    day.date -> {
                        textView.setTextColor(
                            ResourcesCompat.getColor(
                                context.resources,
                                R.color.primaryDarkColor,
                                null
                            )
                        )
                        textView.background = ResourcesCompat.getDrawable(
                            context.resources,
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
                            context.resources,
                            R.color.primaryColor,
                            null
                        )
                    )
                } else {
                    container.textView.setTextColor(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.secondaryDarkColor,
                            null
                        )
                    )
                }
            }

            override fun create(view: View) = TaskFragment.DayViewContainer(view)
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

        val timeCalendar = Calendar.getInstance()

        //
        timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                context,
                { view, hourOfDay, minute ->
                    timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    timeCalendar.set(Calendar.MINUTE, minute)
                    timeCalendar.set(Calendar.SECOND, 0)

                    calendarEventsListener.onSelectTime(timeCalendar)

                }, timeCalendar.get(Calendar.HOUR), timeCalendar.get(Calendar.MINUTE), true
            )
            timePickerDialog.show()
        }

        //
        val reminderCalendar = Calendar.getInstance()
        reminderButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                context,
                { view, hourOfDay, minute ->
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    reminderCalendar.set(Calendar.MINUTE, minute)
                    reminderCalendar.set(Calendar.SECOND, 0)

                    calendarEventsListener.onSelectReminder(reminderCalendar)

                }, reminderCalendar.get(Calendar.HOUR), reminderCalendar.get(Calendar.MINUTE), true
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
        fun selectCalendarDay(stepView: View, calendarSteps: TaskFragment.CalendarSteps) {
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
                TaskFragment.CalendarSteps.Day -> today
                TaskFragment.CalendarSteps.ThreeDay -> today.plusDays(3)
                TaskFragment.CalendarSteps.Week -> today.plusDays(7)
            }

            calendarView.scrollToMonth(todayMonth)
            calendarView.notifyCalendarChanged()
        }

        todayButton.setOnClickListener {
            selectCalendarDay(it, TaskFragment.CalendarSteps.Day)
        }

        threeDaysButton.setOnClickListener {
            selectCalendarDay(it, TaskFragment.CalendarSteps.ThreeDay)
        }

        show()
    }
}
