package com.zaka7024.todody.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.zaka7024.todody.R
import com.zaka7024.todody.databinding.FragmentTodoEditorBinding

class TodoEditor : Fragment(R.layout.fragment_todo_editor) {

    private lateinit var binding: FragmentTodoEditorBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTodoEditorBinding.bind(view)

        val args = TodoEditorArgs.fromBundle(requireArguments())

        binding.apply {
            val todo = args.todo
            todoEdittext.setText(todo.title)
        }
    }
}
