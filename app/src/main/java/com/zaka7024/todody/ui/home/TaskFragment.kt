package com.zaka7024.todody.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.zaka7024.todody.R
import com.zaka7024.todody.databinding.FragmentTaskBinding


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

            //
            val window: Window = window!!

            val animation = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_bottom)

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
}
