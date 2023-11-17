package com.example.budgetapp.adaptor

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.UI.Activities.CategoryActivity
import com.example.budgetapp.databinding.CategoryLayoutBinding
import com.example.budgetapp.models.Category
import com.example.budgetapp.models.Transaction

class AccountActivityAdaptor(
    var context: Context,
    private var categories: ArrayList<Category>,
    var transaction: HashMap<String, Double?>
) : RecyclerView.Adapter<AccountActivityAdaptor.AccountActivityViewHolder>() {

    private var currentProgress: Double? = 0.0

    inner class AccountActivityViewHolder(val binding: CategoryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountActivityViewHolder {
        val binding = CategoryLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return AccountActivityViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: AccountActivityViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryText.text = category.categoryName
        holder.binding.categoryIcon.setImageResource(category.categoryImage)
        holder.binding.categoryIcon.backgroundTintList =
            context.getColorStateList(category.categoryColor)

        when (category.categoryName) {
            "Business" -> {
                currentProgress =
                    transaction[category.categoryName]?.times(100)?.div(category.limit!!)
                if(currentProgress!! >100){
                    val result=(currentProgress!! /100)
                    holder.binding.totalPercent.text = (currentProgress!!.div(result)).toString() + "%"
                }
                else
                    holder.binding.totalPercent.text = currentProgress.toString() + "%"

                holder.binding.progressBar.progress = currentProgress?.toInt()!!
                holder.binding.progressBar.max = 100
            }

            "Investment" -> {
                currentProgress =
                    transaction[category.categoryName]?.times(100)?.div(category.limit!!)
                if(currentProgress!! >100){
                    val result=(currentProgress!! /100)
                    holder.binding.totalPercent.text = (currentProgress!!.div(result)).toString() + "%"
                }
                else
                    holder.binding.totalPercent.text = currentProgress.toString() + "%"
                holder.binding.progressBar.progress = currentProgress?.toInt()!!
                holder.binding.progressBar.max = 100
            }

            "Loan" -> {
                currentProgress =
                    transaction[category.categoryName]?.times(100)?.div(category.limit!!)

                if(currentProgress!! >100){
                    val result=(currentProgress!! /100)
                    holder.binding.totalPercent.text = (currentProgress!!.div(result)).toString() + "%"
                }
                else
                    holder.binding.totalPercent.text = currentProgress.toString() + "%"
                holder.binding.progressBar.progress = currentProgress?.toInt()!!
                holder.binding.progressBar.max = 100
            }

            "Rent" -> {
                currentProgress =
                    transaction[category.categoryName]?.times(100)?.div(category.limit!!)
                if(currentProgress!! >100){
                    val result=(currentProgress!! /100)
                    holder.binding.totalPercent.text = (currentProgress!!.div(result)).toString() + "%"
                }
                else
                    holder.binding.totalPercent.text = currentProgress.toString() + "%"
                holder.binding.progressBar.progress = currentProgress?.toInt()!!
                holder.binding.progressBar.max = 100
            }

            "Other" -> {
                currentProgress =
                    transaction[category.categoryName]?.times(100)?.div(category.limit!!)
                if(currentProgress!! >100){
                    val result=(currentProgress!! /100)
                    holder.binding.totalPercent.text = (currentProgress!!.div(result)).toString() + "%"
                }
                else
                    holder.binding.totalPercent.text = currentProgress.toString() + "%"
                holder.binding.progressBar.progress = currentProgress?.toInt()!!
                holder.binding.progressBar.max = 100
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CategoryActivity::class.java)
            intent.putExtra("category", categories[position].categoryName)
            context.startActivity(intent)
        }

    }
}