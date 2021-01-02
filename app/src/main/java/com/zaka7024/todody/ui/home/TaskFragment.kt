package com.zaka7024.todody.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.zaka7024.todody.R
import com.zaka7024.todody.databinding.FragmentTaskBinding

class TaskFragment : Fragment(R.layout.fragment_task) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)
        binding.textView.text = "Home Fragment"
    }
}