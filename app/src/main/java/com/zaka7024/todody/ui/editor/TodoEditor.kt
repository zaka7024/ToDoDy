package com.zaka7024.todody.ui.editor

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zaka7024.todody.TodoSublistAdapter
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.databinding.FragmentTodoEditorBinding
import com.zaka7024.todody.ui.TodoEditorArgs
import com.zaka7024.todody.ui.task.TaskFragment
import com.zaka7024.todody.ui.task.showCategoryPopup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TodoEditor : Fragment(R.layout.fragment_todo_editor) {

    private lateinit var binding: FragmentTodoEditorBinding
    private lateinit var subitemAdapter: TodoSublistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTodoEditorBinding.bind(view)
        val args = TodoEditorArgs.fromBundle(requireArguments())

        val subitems = args.todo.subitems

        subitemAdapter = TodoSublistAdapter(subitems)
        subitemAdapter.onSubitemEventsListener = object : TodoSublistAdapter.OnSubitemEventsListener {
            override fun onClickDelete(itemPosition: Int) {

            }

            override fun onClickEnter() {

            }

            override fun onTextChange(itemPosition: Int, text: String) {

            }

            override fun onComplete(subitem: Subitem) {

            }
        }

        binding.apply {
            val todoItem = args.todo
            todoEdittext.setText(todoItem.todo.title)

            todoSublistRv.adapter = subitemAdapter
            todoSublistRv.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL, false
            )

            addSubitem.setOnClickListener {
                subitems.add(Subitem(item = "Item"))
                subitemAdapter.notifyItemInserted(subitems.size - 1)
            }

            date.text = "${todoItem.todo.date.toString()}:${todoItem.todo.time.toString()}"
            reminder.text = todoItem.todo.reminderTime.toString()
            //
            val categories = mutableListOf<Category>()

            category.setOnClickListener {
                GlobalScope.launch {
                    showCategoryPopup(requireContext(),categories, it,
                        object : TaskFragment.CategoryPopupEventListener {
                            override fun onSelectCategory(categoryName: String) {

                            }

                            override fun onCategoryAddButtonClick(popupMenu: PopupMenu) {

                            }
                        })
                }
            }
        }
    }
}
