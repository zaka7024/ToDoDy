package com.zaka7024.todody.ui.editor

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.zaka7024.todody.TodoSublistAdapter
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.databinding.FragmentTodoEditorBinding
import com.zaka7024.todody.ui.task.TaskFragment
import com.zaka7024.todody.ui.task.showCategoryPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TodoEditorFragment : Fragment(R.layout.fragment_todo_editor) {

    private lateinit var binding: FragmentTodoEditorBinding
    private lateinit var subitemAdapter: TodoSublistAdapter
    private val editorViewModel by viewModels<EditorViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTodoEditorBinding.bind(view)
        val args = TodoEditorFragmentArgs.fromBundle(requireArguments())

        val subitems = args.todo.subitems

        subitemAdapter = TodoSublistAdapter(subitems)
        subitemAdapter.onSubitemEventsListener = object : TodoSublistAdapter.OnSubitemEventsListener {
            override fun onClickDelete(itemPosition: Int) {
                val subitem = subitems[itemPosition]
                subitems.removeAt(itemPosition)
                subitemAdapter.notifyItemRemoved(itemPosition)
                editorViewModel.removeSubitem(subitem)
            }

            override fun onClickEnter() {

            }

            override fun onTextChange(itemPosition: Int, text: String) {
                val subitem = subitems[itemPosition]
                subitem.item = text
                editorViewModel.updateSubitem(subitem)
            }

            override fun onComplete(subitem: Subitem) {
                editorViewModel.updateSubitem(subitem)
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

            //
            todoEdittext.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    args.todo.todo.title = s.toString()
                    editorViewModel.updateTodo(args.todo.todo)
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            //
            addSubitem.setOnClickListener {
                val subitem = Subitem(item = "")
                subitems.add(subitem)
                subitemAdapter.notifyItemInserted(subitems.size - 1)

                subitem.todoOwnerId = args.todo.todo.todoId
                editorViewModel.addSubitem(subitem)
            }

            date.text = "${todoItem.todo.date.toString()}:${todoItem.todo.time.toString()}"
            reminder.text = todoItem.todo.reminderTime.toString()

            //
            editorViewModel.categories.observe(viewLifecycleOwner) {
                userCategories->
                if (userCategories != null) {
                    category.setOnClickListener {
                        showCategoryPopup(requireContext(), userCategories, it,
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
}
