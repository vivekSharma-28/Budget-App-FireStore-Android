package com.example.budgetapp.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.databinding.SampleCategoryItemBinding
import com.example.budgetapp.models.Category

class CategoryAdapter(
    private var context: Context?,
    private var categories: ArrayList<Category>,
    private var categoryClickListener: CategoryClickListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder?>() {

    interface CategoryClickListener{
        fun onCategoryClick(category: Category)
    }

    inner class CategoryViewHolder(val binding:SampleCategoryItemBinding ) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding=SampleCategoryItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categories=categories[position]
        holder.binding.categoryText.text=categories.categoryName
        holder.binding.categoryIcon.setImageResource(categories.categoryImage)
        holder.binding.categoryIcon.backgroundTintList=context?.getColorStateList(categories.categoryColor)
        holder.itemView.setOnClickListener {
            categoryClickListener.onCategoryClick(categories)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

}