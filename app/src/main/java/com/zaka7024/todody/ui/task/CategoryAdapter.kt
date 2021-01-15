package com.zaka7024.todody.ui.task

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.zaka7024.todody.R
import com.zaka7024.todody.data.Category
import com.zaka7024.todody.databinding.CategoryItemBinding

class CategoryAdapter(
    private val categories: List<Category>,
    private val setOnCategoryClickListener: SetOnCategoryClickListener? = null
) :
    RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {

    private var currentSelectedIndex = 0

    interface SetOnCategoryClickListener {
        fun onClick(category: Category)
    }

    inner class CategoryHolder(
        private val binding: CategoryItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, position: Int) {
            binding.apply {
                categoryText.text = category.categoryName

                if(position == currentSelectedIndex) {
                    categoryText.backgroundTintList = ResourcesCompat.getColorStateList(root.resources, R.color.primaryColor, null)
                    categoryText.setTextColor(ResourcesCompat.getColorStateList(root.resources, R.color.primaryLightColor, null))
                }else {
                    categoryText.backgroundTintList = ResourcesCompat.getColorStateList(root.resources, R.color.secondaryLightColor, null)
                    categoryText.setTextColor(ResourcesCompat.getColorStateList(root.resources, R.color.primaryDarkColor, null))
                }

                root.setOnClickListener {
                    currentSelectedIndex = position
                    Log.i("taskViewModel", "currentSelectedIndex: $currentSelectedIndex")
                    Log.i("taskViewModel", "position: $position")
                    setOnCategoryClickListener?.onClick(category)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, position)
    }

    override fun getItemCount() = categories.size
}
