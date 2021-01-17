package com.zaka7024.todody.ui.task

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthScrollListener
import com.kizitonwose.calendarview.ui.ViewContainer
import com.zaka7024.todody.R
import com.zaka7024.todody.TodoSublistAdapter
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.data.Todo
import com.zaka7024.todody.data.TodosWithSubitems
import com.zaka7024.todody.databinding.CalendarDayLayoutBinding
import com.zaka7024.todody.databinding.FragmentTaskBinding
import com.zaka7024.todody.utils.AlarmReceiver
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

        private val binding = CalendarDayLayoutBinding.bind(view)
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

        lifecycle.addObserver(taskViewModel)

        val binding = FragmentTaskBinding.bind(view)

        val todos = mutableListOf<TodosWithSubitems>()
        val categories = mutableListOf<Category>()
        val otherTodos = mutableListOf<TodosWithSubitems>()

        // Setup all adapters
        setupTodoAdapter(todos)
        setupOthersAdapter(otherTodos)
        setupCategoryAdapter(binding, categories)

        // Setup all recycler views
        setupTodayRecyclerView(binding, todos)
        setupOthersRecyclerView(binding, otherTodos)
        setupCategoryRecyclerView(binding)

        // Get all todos form the selected category for today and notify the adapter
        getAllTodayTodos(binding, todos)

        // Get all todos form the selected category for others days and notify the adapter
        getAllOtherTodos(binding, otherTodos)

        // Get all categories and enable add t,odo button
        getAllCategories(binding, categories, todos)

    }

    private fun getAllCategories(
        binding: FragmentTaskBinding, categories: MutableList<Category>,
        todos: MutableList<TodosWithSubitems>
    ) {
        taskViewModel.categories.observe(viewLifecycleOwner, { userCategories ->
            if (userCategories != null) {
                categoryAdapter.notifyItemRangeRemoved(0, categories.size)
                categories.clear()
                categories.addAll(userCategories)
                categoryAdapter.notifyDataSetChanged()

                // Add new t,odo to the database
                binding.addTask.setOnClickListener {
                    showCreateTodoDialog(
                        requireContext(),
                        userCategories,
                        object : TodoCreateListener {
                            override fun onSend(
                                todo: Todo,
                                subitems: List<Subitem>,
                                categoryName: String
                            ) {
                                if (todo.title.isEmpty()) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Please enter some thing",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    taskViewModel.saveTodo(
                                        todo,
                                        subitems.toTypedArray(),
                                        categoryName
                                    )
                                    todayTodoAdapter.notifyItemInserted(todos.size - 1)
                                    // Create notification
                                    if (todo.reminderTime != null) {
                                        val calendar = Calendar.getInstance()
                                        calendar.time = todo.reminderTime!!
                                        startAlarm(todo, calendar)
                                    }

                                }
                            }

                            override fun onCreateCategory(categoryName: String) {
                                taskViewModel.saveCategory(categoryName)
                            }
                        })
                }
            }
        })
    }

    private fun getAllOtherTodos(
        binding: FragmentTaskBinding,
        otherTodos: MutableList<TodosWithSubitems>
    ) {
        taskViewModel.otherTodos.observe(viewLifecycleOwner, { userOtherTodos ->
            otherTodos.clear()
            otherTodos.addAll(userOtherTodos)
            othersTodoAdapter.notifyDataSetChanged()

            binding.otherNoTasksHint.isVisible = otherTodos.isEmpty()
        })
    }

    private fun getAllTodayTodos(
        binding: FragmentTaskBinding,
        todos: MutableList<TodosWithSubitems>
    ) {
        taskViewModel.todayTodos.observe(viewLifecycleOwner, { userTodos ->
            todos.clear()
            todos.addAll(userTodos)
            todayTodoAdapter.notifyDataSetChanged()

            binding.todayRv.adapter = todayTodoAdapter
            binding.todayNoTasksHint.isVisible = userTodos.isEmpty()
        })
    }

    private fun setupCategoryRecyclerView(binding: FragmentTaskBinding) {
        binding.apply {
            categoryRv.adapter = categoryAdapter
            categoryRv.layoutManager =
                WrapContentLinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }
    }

    private fun setupOthersRecyclerView(
        binding: FragmentTaskBinding,
        otherTodos: MutableList<TodosWithSubitems>
    ) {
        binding.apply {
            otherRv.adapter = othersTodoAdapter
            otherRv.layoutManager =
                WrapContentLinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            ItemTouchHelper(ItemTouchHelperCallback(
                requireContext()
            ) { position ->
                val todo = otherTodos.removeAt(position)
                taskViewModel.removeTodo(todo.todo)
                othersTodoAdapter.notifyItemRemoved(position)
            }
            ).attachToRecyclerView(binding.otherRv)
        }
    }

    private fun setupTodayRecyclerView(
        binding: FragmentTaskBinding,
        todos: MutableList<TodosWithSubitems>
    ) {
        binding.apply {
            todayRv.adapter = todayTodoAdapter
            todayRv.layoutManager =
                WrapContentLinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
            ItemTouchHelper(ItemTouchHelperCallback(
                requireContext()
            ) { position ->
                val todo = todos.removeAt(position)
                taskViewModel.removeTodo(todo.todo)
                todayTodoAdapter.notifyItemRemoved(position)
            }
            ).attachToRecyclerView(binding.todayRv)
        }
    }

    private fun setupTodoAdapter(todos: MutableList<TodosWithSubitems>) {
        todayTodoAdapter = TodoAdapter(todos, object : TodoAdapter.OnTodoHolderEventsListener {
            override fun onClick(todoItem: TodosWithSubitems) {
                findNavController().navigate(
                    TaskFragmentDirections.actionTaskFragmentToTodoEditor2(
                        todoItem
                    )
                )
            }

            override fun onCompleteTodo(todoItem: TodosWithSubitems) {
                taskViewModel.updateTodo(todoItem.todo)
            }
        })
    }

    private fun setupOthersAdapter(otherTodos: MutableList<TodosWithSubitems>) {
        othersTodoAdapter =
            TodoAdapter(otherTodos, object : TodoAdapter.OnTodoHolderEventsListener {
                override fun onClick(todoItem: TodosWithSubitems) {
                    findNavController().navigate(
                        TaskFragmentDirections.actionTaskFragmentToTodoEditor2(
                            todoItem
                        )
                    )
                }

                override fun onCompleteTodo(todoItem: TodosWithSubitems) {
                    taskViewModel.updateTodo(todoItem.todo)
                }
            })
    }

    private fun setupCategoryAdapter(binding: FragmentTaskBinding, categories: List<Category>) {
        categoryAdapter =
            CategoryAdapter(categories, object : CategoryAdapter.SetOnCategoryClickListener {
                override fun onClick(category: Category) {
                    taskViewModel.setCurrentCategory(category)
                    binding.apply {
                        categoryRv.adapter = categoryAdapter
                        categoryRv.smoothScrollToPosition(categories.indexOf(category))
                    }
                }
            })
    }

    private fun startAlarm(todo: Todo, calendar: Calendar) {
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("message", todo.title)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, 0)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    interface TodoCreateListener {
        fun onSend(todo: Todo, subitems: List<Subitem>, categoryName: String)
        fun onCreateCategory(categoryName: String)
    }

    interface CalendarEventsListener {
        fun onSelectTime(calendar: Calendar?)
        fun onSelectReminder(calendar: Calendar?)
        fun onSelectDate(localDate: LocalDate)
    }

    interface CategoryPopupEventListener {
        fun onSelectCategoryName(categoryName: String)
        fun onCategoryAddButtonClick(popupMenu: PopupMenu)
    }

    enum class CalendarSteps {
        Day, ThreeDay, Week
    }
}

fun showCreateTodoDialog(
    context: Context,
    categories: List<Category>,
    todoCreateListener: TaskFragment.TodoCreateListener,
    defaultDate: LocalDate? = null
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
        val adapter = TodoSublistAdapter(subTodoList)

        adapter.onSubitemEventsListener =
            object : TodoSublistAdapter.OnSubitemEventsListener {
                override fun onClickDelete(itemPosition: Int) {
                    // Remove the subitem from the RecyclerView
                    subTodoList.removeAt(itemPosition)
                    adapter.notifyItemRemoved(itemPosition)
                    adapter.notifyItemRangeChanged(itemPosition, subTodoList.size)
                }

                // Function called when keyboard enter button clicked
                override fun onClickEnter() {
                    subTodoList.add(Subitem(item = ""))
                    adapter.notifyItemInserted(subTodoList.size - 1)
                    sublistRecyclerView.scrollToPosition(subTodoList.size - 1)
                }

                override fun onTextChange(itemPosition: Int, text: String) {
                    subTodoList[itemPosition].item = text
                }

                override fun onComplete(subitem: Subitem) {

                }
            }

        sublistRecyclerView.adapter = adapter

        // Add the first subitem
        val todoListButton = findViewById<ImageView>(R.id.todo_list)
        todoListButton.setOnClickListener {
            subTodoList.add(Subitem(item = ""))
            adapter.notifyItemInserted(subTodoList.size - 1)
            sublistRecyclerView.scrollToPosition(subTodoList.size - 1)
        }

        // Category menu
        val todoCategoryButton = findViewById<TextView>(R.id.todo_category)
        var selectedCategory = "Home"
        todoCategoryButton.text = selectedCategory

        todoCategoryButton.setOnClickListener {
            showCategoryPopup(
                context,
                categories,
                it,
                object : TaskFragment.CategoryPopupEventListener {
                    override fun onSelectCategoryName(categoryName: String) {
                        selectedCategory = categoryName
                        todoCategoryButton.alpha = 0f
                        todoCategoryButton.animate().alpha(1f).duration = 400
                        todoCategoryButton.text = categoryName
                        selectedCategory = todoCategoryButton.text.toString()
                    }

                    override fun onCategoryAddButtonClick(popupMenu: PopupMenu) {
                        showCreateCategoryDialog(context, popupMenu, todoCreateListener)
                    }
                })
        }

        var time: Calendar? = null
        var reminderTime: Calendar? = null
        var selectedDate: LocalDate? = null

        // Show calender view
        val todoCalender = findViewById<ImageView>(R.id.todo_calendar)
        todoCalender.setOnClickListener {
            showCalendar(context, object : TaskFragment.CalendarEventsListener {
                override fun onSelectTime(calendar: Calendar?) {
                    time = calendar
                }

                override fun onSelectReminder(calendar: Calendar?) {
                    if (calendar != null) {
                        reminderTime?.apply {
                            set(Calendar.YEAR, selectedDate?.dayOfYear!!)
                            set(Calendar.MONTH, selectedDate?.month!!.value)
                            set(Calendar.DAY_OF_MONTH, selectedDate?.dayOfMonth!!)
                            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                        }
                    } else {
                        reminderTime = calendar
                    }
                }

                override fun onSelectDate(localDate: LocalDate) {
                    selectedDate = localDate
                    if(reminderTime != null) {
                        reminderTime?.apply {
                            set(Calendar.YEAR, localDate.dayOfYear)
                            set(Calendar.MONTH, localDate.month.value)
                            set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
                        }
                    }
                }
            }, defaultDate)
        }

        // Send the todo
        val sendTodoButton = findViewById<ImageView>(R.id.todo_send)
        sendTodoButton.setOnClickListener {
            todoCreateListener.onSend(
                Todo(
                    title = todoEditText.text.toString(),
                    time = time?.time,
                    reminderTime = reminderTime?.time,
                    date = selectedDate,
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

fun showCalendar(
    context: Context,
    calendarEventsListener: TaskFragment.CalendarEventsListener,
    defaultDate: LocalDate? = null
) {

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
        var currentSelectedDay = defaultDate ?: today
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
        val selectedTime = findViewById<TextView>(R.id.selectedTime)
        val selectedReminder = findViewById<TextView>(R.id.selectedReminder)

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

                when (currentSelectedDay) {
                    day.date -> {
                        textView.setTextColor(
                            ResourcesCompat.getColor(
                                context.resources,
                                R.color.primaryLightColor,
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
            }

            override fun create(view: View) = TaskFragment.DayViewContainer(view)
        }

        // Dismiss the dialog
        val cancelButton = findViewById<TextView>(R.id.cancelButton)
        val doneButton = findViewById<TextView>(R.id.doneButton)
        cancelButton.setOnClickListener {
            calendarEventsListener.onSelectReminder(null)
            calendarEventsListener.onSelectTime(null)
            dismiss()
        }

        doneButton.setOnClickListener {
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

                    //
                    val timeString = "${timeCalendar.get(Calendar.HOUR_OF_DAY)}:${
                        timeCalendar.get(Calendar.MINUTE)
                    }"
                    selectedTime.text = timeString


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

                    val timeString = "${reminderCalendar.get(Calendar.HOUR_OF_DAY)}:${
                        reminderCalendar.get(Calendar.MINUTE)
                    }"
                    selectedReminder.text = timeString

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

fun showCategoryPopup(
    context: Context,
    categories: List<Category>,
    anchor: View,
    categoryPopupEventListener: TaskFragment.CategoryPopupEventListener
) {

    val popupMenu = PopupMenu(context, anchor)
    val menu = popupMenu.menu
    //TODO:: Add icon to the (New Category) Item
    popupMenu.inflate(R.menu.category_menu)
    for (i in 0.until(categories.size)) {
        val category = categories[i]
        menu.add(category.categoryName)
    }
    popupMenu.setOnMenuItemClickListener { item ->
        if (item.itemId == R.id.add_category_item) {
            categoryPopupEventListener.onCategoryAddButtonClick(popupMenu)
        } else {
            categoryPopupEventListener.onSelectCategoryName(item.title.toString())
        }
        true
    }
    popupMenu.show()
}

fun showCreateCategoryDialog(
    context: Context,
    popupMenu: PopupMenu,
    todoCreateListener: TaskFragment.TodoCreateListener
) {

    val dialog = Dialog(context)

    dialog.apply {
        setContentView(R.layout.category_dialog)

        val window = window!!
        window.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancelButton = findViewById<TextView>(R.id.cancel)
        cancelButton.setOnClickListener {
            dismiss()
        }

        val saveButton = findViewById<TextView>(R.id.save)
        val categoryEditText = findViewById<EditText>(R.id.category_name)
        categoryEditText.requestFocus()

        saveButton.setOnClickListener {
            val categoryText = categoryEditText.text.toString()
            if (categoryEditText.text.toString().trimEnd().isEmpty()) {
                Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            todoCreateListener.onCreateCategory(categoryText)
            // Add the new item
            popupMenu.menu.add(categoryText)
            popupMenu.dismiss()
            popupMenu.show()

            dismiss()
        }

        show()
    }
}
