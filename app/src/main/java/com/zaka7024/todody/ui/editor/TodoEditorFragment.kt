package com.zaka7024.todody.ui.editor

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import com.zaka7024.todody.TodoSublistAdapter
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.data.Subitem
import com.zaka7024.todody.databinding.FragmentTodoEditorBinding
import com.zaka7024.todody.ui.task.TaskFragment
import com.zaka7024.todody.ui.task.showCategoryPopup
import com.zaka7024.todody.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_todo_editor.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class TodoEditorFragment : Fragment(R.layout.fragment_todo_editor) {

    private lateinit var binding: FragmentTodoEditorBinding
    private lateinit var subitemAdapter: TodoSublistAdapter
    private val editorViewModel by viewModels<EditorViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTodoEditorBinding.bind(view)
        val args = TodoEditorFragmentArgs.fromBundle(requireArguments())

        editorViewModel.getCategory(args.todo.todo.categoryOwnerId!!)

        val subitems = args.todo.subitems

        subitemAdapter = TodoSublistAdapter(subitems)
        subitemAdapter.onSubitemEventsListener =
            object : TodoSublistAdapter.OnSubitemEventsListener {
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

        //
        editorViewModel.currentCategory.observe(viewLifecycleOwner) { currentCategory ->
            category.text = currentCategory.categoryName
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
                lifecycleScope.launch {
                    val subitem = Subitem(item = "")
                    val id = editorViewModel.addSubitem(subitem)
                    subitem.id = id
                    subitems.add(subitem)
                    subitemAdapter.notifyItemInserted(subitems.size - 1)
                    subitem.todoOwnerId = args.todo.todo.todoId
                }
            }

            //
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            deleteButton.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setMessage("Do you want to DELETE this Todo?")
                    .setNegativeButton(
                        "CANCEL"
                    ) { dialog, which ->
                        dialog.dismiss()
                    }.setPositiveButton("DELETE") { dialog, which ->
                        editorViewModel.removeTodo(todoItem.todo)
                        dialog.dismiss()
                        findNavController().navigateUp()
                    }.show()
            }

            //
            if (todoItem.todo.time != null) {
                date.text = "${todoItem.todo.date.toString()} : ${Utils.getDateTimeIn24Format(todoItem.todo.time as Date)}"
            } else {
                date.text = "${todoItem.todo.date.toString()}"
            }

            if (todoItem.todo.reminderTime != null) {
                reminder.text = "${todoItem.todo.date.toString()} : ${Utils.getDateTimeIn24Format(todoItem.todo.reminderTime as Date)}"
            } else {
                reminder.text = getString(R.string.no_reminder)
            }

            //
            editorViewModel.categories.observe(viewLifecycleOwner) { userCategories ->
                if (userCategories != null) {
                    //
                    category.animate().alpha(1f).duration = 400
                    category.setOnClickListener {
                        showCategoryPopup(requireContext(), userCategories, it,
                            object : TaskFragment.CategoryPopupEventListener {
                                override fun onSelectCategoryName(categoryName: String) {
                                    editorViewModel.updateTodoCategory(todoItem.todo, categoryName)
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
