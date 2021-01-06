package com.zaka7024.todody.ui.home

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthScrollListener
import com.kizitonwose.calendarview.ui.ViewContainer
import com.zaka7024.todody.R
import com.zaka7024.todody.databinding.CalendarDayLayoutBinding
import com.zaka7024.todody.databinding.FragmentTaskBinding
import kotlinx.android.synthetic.main.todo_calendar_layout.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*


class TaskFragment : Fragment(R.layout.fragment_task) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)
        binding.textView.text = "Home Fragment"

        binding.addTask.setOnClickListener {
            showCreateTodoDialog()
        }
    }

    private fun showCreateTodoDialog() {
        val dialog = Dialog(requireContext())
        dialog.apply {
            //
            setContentView(R.layout.create_todo_layout)

            val todoCalender = findViewById<ImageView>(R.id.todo_calendar)
            todoCalender.setOnClickListener {
                showCalendar(object : CalendarEventsListener {
                    override fun onSelectTime(calendar: Calendar) {
                        Log.i("TaskFragment", calendar.toString())
                    }

                    override fun onSelectReminder(calendar: Calendar) {
                    }

                    override fun onSelectDate(localDate: LocalDate) {
                    }
                })
            }

            //
            val window: Window = window!!

            val animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_in_bottom
            )
            dialog.findViewById<ConstraintLayout>(R.id.todo_create_view).startAnimation(animation)

            window.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val wlp = window.attributes
            wlp.gravity = Gravity.BOTTOM
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = wlp



            // Show
            show()
        }
    }

    interface CalendarEventsListener {
        fun onSelectTime(calendar: Calendar)
        fun onSelectReminder(calendar: Calendar)
        fun onSelectDate(localDate: LocalDate)
    }

    enum class CalendarSteps{
        Day, ThreeDay, Week
    }

    private fun showCalendar(calendarEventsListener: CalendarEventsListener) {


        val dialog = Dialog(requireContext())
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

                    if(day.owner == DayOwner.THIS_MONTH) {
                        container.textView.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.primaryColor,
                                null
                            )
                        )
                    }else {
                        container.textView.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.secondaryDarkColor,
                                null
                            )
                        )
                    }
                }
                override fun create(view: View) =  DayViewContainer(view)
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
                if(currentMonth > YearMonth.now()) {
                    updateCurrentMonth(currentMonth.minusMonths(1))
                }
            }

            // Move to next month if exists
            nextMonthButton.setOnClickListener {
                if(currentMonth < YearMonth.now().plusMonths(monthsToAdd)) {
                    updateCurrentMonth(currentMonth.plusMonths(1))
                }
            }

            val calendar = Calendar.getInstance()

            //
            timeButton.setOnClickListener {
                val timePickerDialog = TimePickerDialog(requireContext(),
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
                val timePickerDialog = TimePickerDialog(requireContext(),
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
                    if(view != stepView) {
                        view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.secondaryDarkColor)
                        view.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.primaryTextColor))
                    }else{
                        view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primaryDarkColor)
                        view.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.primaryLightColor))
                    }
                }

                currentSelectedDay = when(calendarSteps) {
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
