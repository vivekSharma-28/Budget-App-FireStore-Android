package com.example.budgetapp.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.databinding.RowAccountBinding
import com.example.budgetapp.databinding.RowTransactionBinding
import com.example.budgetapp.databinding.SampleCategoryItemBinding
import com.example.budgetapp.models.Account
import com.example.budgetapp.models.Category

class AccountAdaptor(private var context: Context?, private var transactionMethodArrayList: ArrayList<Account>,private var accountClickListener: AccountClickListener):RecyclerView.Adapter<AccountAdaptor.AccountViewHolder>()
{
    interface AccountClickListener{
        fun onAccountClick(account: Account)
    }

    inner class AccountViewHolder(val binding: RowAccountBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding=RowAccountBinding.inflate(LayoutInflater.from(context),parent,false)
        return AccountViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transactionMethodArrayList.size
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account=transactionMethodArrayList[position]
        holder.binding.accountName.text=account.accountName
        holder.itemView.setOnClickListener {
            accountClickListener.onAccountClick(account)
        }
    }
}