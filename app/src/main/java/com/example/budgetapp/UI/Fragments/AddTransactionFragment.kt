package com.example.budgetapp.UI.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.R
import com.example.budgetapp.UI.Activities.AccountsActivity
import com.example.budgetapp.UI.Activities.MainActivity
import com.example.budgetapp.models.Transaction
import com.example.budgetapp.Utils.Constants
import com.example.budgetapp.Utils.Helper
import com.example.budgetapp.adaptor.AccountAdaptor
import com.example.budgetapp.adaptor.CategoryAdapter
import com.example.budgetapp.databinding.FragmentAddTransactionBinding
import com.example.budgetapp.databinding.ListDialogBinding
import com.example.budgetapp.models.Account
import com.example.budgetapp.models.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date
import java.util.UUID

class AddTransactionFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddTransactionBinding

    private var transaction: Transaction? = null

    private var type: String?=null

    private var date: Date? = null

    private var check=true

    private var isChecked:String?=null

    private var isType:String?=null

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTransactionBinding.inflate(layoutInflater)
        transaction = Transaction()

        isChecked=arguments?.getString("key")

        isType=arguments?.getString("type")

        binding.incomeBtn.setOnClickListener {
            binding.incomeBtn.background = context?.getDrawable(R.drawable.income_selector)
            binding.expenseBtn.background = context?.getDrawable(R.drawable.default_selector)
            context?.getColor(R.color.textColor)
                ?.let { it1 -> binding.expenseBtn.setTextColor(it1) }
            context?.getColor(R.color.greenColor)
                ?.let { it1 -> binding.incomeBtn.setTextColor(it1) }
            type = Constants.INCOME
        }

        binding.expenseBtn.setOnClickListener {
            binding.incomeBtn.background = context?.getDrawable(R.drawable.default_selector)
            binding.expenseBtn.background = context?.getDrawable(R.drawable.expense_selector)
            context?.getColor(R.color.textColor)?.let { it1 -> binding.incomeBtn.setTextColor(it1) }
            context?.getColor(R.color.redColor)?.let { it1 -> binding.expenseBtn.setTextColor(it1) }
            type = Constants.EXPENSE
        }

        binding.date.setOnClickListener {

            val datePickerDialog = DatePickerDialog(requireContext())
            datePickerDialog.setOnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                val calendar = Calendar.getInstance()
                calendar[Calendar.DAY_OF_MONTH] = datePicker.dayOfMonth
                calendar[Calendar.MONTH] = datePicker.month
                calendar[Calendar.YEAR] = datePicker.year

                val dateToShow = Helper.formatDate(calendar.time)
                date = calendar.time
                binding.date.setText(dateToShow)
                check=false
            }
            datePickerDialog.show()

        }

        binding.category.setOnClickListener { c ->
            val dialogBinding: ListDialogBinding = ListDialogBinding.inflate(inflater)
            val categoryDialog = AlertDialog.Builder(context).create()
            categoryDialog.setView(dialogBinding.root)

            val categoryAdapter = Constants.categories?.let {
                CategoryAdapter(context,
                    it,
                    categoryClickListener = object : CategoryAdapter.CategoryClickListener {
                        override fun onCategoryClick(category: Category) {
                            binding.category.setText(category.categoryName)
                            categoryDialog.dismiss()
                        }

                    })
            }
            dialogBinding.recyclerView.layoutManager = GridLayoutManager(context, 3)
            dialogBinding.recyclerView.adapter = categoryAdapter
            categoryDialog.show()
        }

        binding.transactionMethod.setOnClickListener {
            val dialogBinding: ListDialogBinding = ListDialogBinding.inflate(inflater)
            val accountDialog = AlertDialog.Builder(context).create()
            accountDialog.setView(dialogBinding.root)

            val accounts = ArrayList<Account>()
            accounts.add(Account(0.0, "Cash"))
            accounts.add(Account(0.0, "Online"))

            val accountAdaptor =
                AccountAdaptor(context, accounts, object : AccountAdaptor.AccountClickListener {
                    override fun onAccountClick(account: Account) {
                        binding.transactionMethod.setText(account.accountName)
                        accountDialog.dismiss()
                    }
                })
            dialogBinding.recyclerView.layoutManager = LinearLayoutManager(context)
            dialogBinding.recyclerView.adapter = accountAdaptor
            accountDialog.show()
        }

        binding.saveTransactionBtn.setOnClickListener {
            createExpense()
        }

        return binding.root
    }

    private fun createExpense() {
        val expenseId = UUID.randomUUID().toString()
        val amount = binding.amount.text.toString().trim()
        val category = binding.category.text.toString().trim()
        val transactionMethod = binding.transactionMethod.text.toString()
        val note = binding.note.text.toString().trim()

        if (amount.isEmpty()|| category.isEmpty() || transactionMethod.isEmpty() || check)
        {
            Toast.makeText(requireContext(),"Please Fill All The Fields ",Toast.LENGTH_SHORT).show()
            return
        }
        else
        {
            val transactionModel =
                Transaction(
                    type = type,
                    category = category,
                    account = transactionMethod,
                    note = note,
                    date = date,
                    amount = amount.toDouble(),
                    id = expenseId,
                    uid = FirebaseAuth.getInstance().uid
                )
            FirebaseFirestore.getInstance().collection("transaction").document(expenseId)
                .set(transactionModel)
            if(isChecked=="category"){
                context?.startActivity(Intent(context,AccountsActivity::class.java))
                activity?.finish()
            }
            else{
                context?.startActivity(Intent(context,MainActivity::class.java))
                activity?.finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(isType!=Constants.EXPENSE)
        {
            binding.incomeBtn.background = context?.getDrawable(R.drawable.income_selector)
            binding.expenseBtn.background = context?.getDrawable(R.drawable.default_selector)
            context?.getColor(R.color.textColor)
                ?.let { it1 -> binding.expenseBtn.setTextColor(it1) }
            context?.getColor(R.color.greenColor)
                ?.let { it1 -> binding.incomeBtn.setTextColor(it1) }
            type = Constants.INCOME
        }
        else
        {
            binding.incomeBtn.background = context?.getDrawable(R.drawable.default_selector)
            binding.expenseBtn.background = context?.getDrawable(R.drawable.expense_selector)
            context?.getColor(R.color.textColor)?.let { it1 -> binding.incomeBtn.setTextColor(it1) }
            context?.getColor(R.color.redColor)?.let { it1 -> binding.expenseBtn.setTextColor(it1) }
            type = Constants.EXPENSE
        }
    }

}