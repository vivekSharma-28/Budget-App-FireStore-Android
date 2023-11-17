package com.example.budgetapp.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.R
import com.example.budgetapp.Utils.Constants
import com.example.budgetapp.Utils.Helper
import com.example.budgetapp.databinding.RowTransactionBinding
import com.example.budgetapp.models.Transaction

class TransactionAdaptor( var context: Context, var transactionClickListener: TransactionClickListener):RecyclerView.Adapter<TransactionAdaptor.TransactionViewHolder>() {

    private var transactionList= ArrayList<Transaction?>()

    interface TransactionClickListener{
        fun onTransactionClick(transaction: Transaction?)
    }

    inner class TransactionViewHolder(var binding: RowTransactionBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding=RowTransactionBinding.inflate(LayoutInflater.from(context),parent,false)
        return TransactionViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return transactionList.size
    }

    fun add(transactionModel: Transaction?) {
        transactionList.add(transactionModel)
        notifyDataSetChanged()
    }

    fun clear() {
        transactionList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction=transactionList[position]
//        val transactionModel=transaction
        holder.binding.transactionAmount.text="Rs. ${transaction?.amount}"
        holder.binding.accountLbl.text=transaction?.account
        holder.binding.transactionDate.text=Helper.formatDate(transaction?.date)
        holder.binding.transactionCategory.text=transaction?.category

        val transactionCategory=Constants.getCategoryDetails(transaction?.category)

        transactionCategory?.categoryImage?.let { holder.binding.categoryIcon.setImageResource(it) }
        holder.binding.categoryIcon.backgroundTintList= transactionCategory?.categoryColor?.let {
            context.getColorStateList(
                it
            )
        }

        holder.binding.accountLbl.backgroundTintList=context.getColorStateList(Constants.getAccountsColor(transaction?.account))

        if(transaction?.type.equals(Constants.INCOME)){
            holder.binding.transactionAmount.setTextColor(context.getColor(R.color.greenColor))
        }
        else if(transaction?.type.equals(Constants.EXPENSE)){
            holder.binding.transactionAmount.setTextColor(context.getColor(R.color.redColor))
        }

        holder.itemView.setOnClickListener {

            transactionClickListener.onTransactionClick(transaction)

        }
    }
}