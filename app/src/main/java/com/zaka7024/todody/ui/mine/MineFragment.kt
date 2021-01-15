package com.zaka7024.todody.ui.mine

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zaka7024.todody.R
import com.zaka7024.todody.databinding.FragmentMineBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MineFragment : Fragment(R.layout.fragment_mine) {
    private val mineViewModel by viewModels<MineViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val biding = FragmentMineBinding.bind(view)

        mineViewModel.completedTodos.observe(viewLifecycleOwner) {
            biding.completedTasks.text = it.toString()
        }

        mineViewModel.pendingTodos.observe(viewLifecycleOwner) {
            biding.pendingTasks.text = it.toString()
        }
    }
}