package com.zaka7024.todody.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zaka7024.todody.CreateTodoSublistAdapter
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.databinding.FragmentTodoEditorBinding

class TodoEditor : Fragment(R.layout.fragment_todo_editor) {

    private lateinit var binding: FragmentTodoEditorBinding
    private lateinit var subitemAdapter: CreateTodoSublistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTodoEditorBinding.bind(view)
        val args = TodoEditorArgs.fromBundle(requireArguments())

        subitemAdapter = CreateTodoSublistAdapter(args.todo.subitems)

        binding.apply {
            val todoItem = args.todo
            todoEdittext.setText(todoItem.todo.title)

            todoSublistRv.adapter = subitemAdapter
            todoSublistRv.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL, false
            )
        }
    }
}
