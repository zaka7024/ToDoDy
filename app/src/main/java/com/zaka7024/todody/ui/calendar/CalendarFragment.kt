package com.zaka7024.todody.ui.calendar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.zaka7024.todody.R
import com.zaka7024.todody.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment(R.layout.fragment_calendar) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCalendarBinding.bind(view)
    }
}