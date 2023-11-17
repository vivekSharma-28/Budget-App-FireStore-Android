package com.example.budgetapp.UI.Activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.R
import com.example.budgetapp.Utils.Constants
import com.example.budgetapp.Utils.Helper
import com.example.budgetapp.adaptor.AccountAdaptor
import com.example.budgetapp.adaptor.CategoryAdapter
import com.example.budgetapp.databinding.ActivityEditTrasactionBinding
import com.example.budgetapp.databinding.ListDialogBinding
import com.example.budgetapp.models.Account
import com.example.budgetapp.models.Category
import com.example.budgetapp.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date
import java.util.UUID

class Edit_Trasaction_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTrasactionBinding
    private lateinit var transactionModel: Transaction
    private var type: String? = null
    private var date: Date? = null
    private var isChecked:String?=null


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTrasactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
        supportActionBar?.title = "Update Transaction"

        transactionModel = intent.getSerializableExtra("model") as Transaction
        isChecked=intent.getStringExtra("key")

        dataSet()

        binding.incomeBtn.setOnClickListener {
            binding.incomeBtn.background = this.getDrawable(R.drawable.income_selector)
            binding.expenseBtn.background = this.getDrawable(R.drawable.default_selector)
            this.getColor(R.color.textColor).let { it1 -> binding.expenseBtn.setTextColor(it1) }
            this.getColor(R.color.greenColor).let { it1 -> binding.incomeBtn.setTextColor(it1) }
            type = Constants.INCOME
        }

        binding.expenseBtn.setOnClickListener {
            binding.incomeBtn.background = this.getDrawable(R.drawable.default_selector)
            binding.expenseBtn.background = this.getDrawable(R.drawable.expense_selector)
            this.getColor(R.color.textColor).let { it1 -> binding.incomeBtn.setTextColor(it1) }
            this.getColor(R.color.redColor).let { it1 -> binding.expenseBtn.setTextColor(it1) }
            type = Constants.EXPENSE
        }

        binding.date.setOnClickListener {

            val datePickerDialog = DatePickerDialog(this)
            datePickerDialog.setOnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                val calendar = Calendar.getInstance()
                calendar[Calendar.DAY_OF_MONTH] = datePicker.dayOfMonth
                calendar[Calendar.MONTH] = datePicker.month
                calendar[Calendar.YEAR] = datePicker.year

                val dateToShow = Helper.formatDate(calendar.time)
                date = calendar.time
                binding.date.setText(dateToShow)
            }
            datePickerDialog.show()

        }

        binding.category.setOnClickListener { c ->
            val dialogBinding: ListDialogBinding = ListDialogBinding.inflate(layoutInflater)
            val categoryDialog = AlertDialog.Builder(this).create()
            categoryDialog.setView(dialogBinding.root)

            val categoryAdapter = Constants.categories?.let {
                CategoryAdapter(this,
                    it,
                    categoryClickListener = object : CategoryAdapter.CategoryClickListener {
                        override fun onCategoryClick(category: Category) {
                            binding.category.setText(category.categoryName)
                            categoryDialog.dismiss()
                        }

                    })
            }
            dialogBinding.recyclerView.layoutManager = GridLayoutManager(this, 3)
            dialogBinding.recyclerView.adapter = categoryAdapter
            categoryDialog.show()
        }

        binding.transactionMethod.setOnClickListener {
            val dialogBinding: ListDialogBinding = ListDialogBinding.inflate(layoutInflater)
            val accountDialog = AlertDialog.Builder(this).create()
            accountDialog.setView(dialogBinding.root)

            val accounts = ArrayList<Account>()
            accounts.add(Account(0.0, "Cash"))
            accounts.add(Account(0.0, "Online"))

            val accountAdaptor =
                AccountAdaptor(this, accounts, object : AccountAdaptor.AccountClickListener {
                    override fun onAccountClick(account: Account) {
                        binding.transactionMethod.setText(account.accountName)
                        accountDialog.dismiss()
                    }
                })
            dialogBinding.recyclerView.layoutManager = LinearLayoutManager(this)
            dialogBinding.recyclerView.adapter = accountAdaptor
            accountDialog.show()
        }

        binding.updateTransactionBtn.setOnClickListener {
            updateExpense()
        }

    }

    private fun dataSet(){

        if (transactionModel.type == Constants.INCOME) {
            binding.incomeBtn.background = this.getDrawable(R.drawable.income_selector)
            binding.expenseBtn.background = this.getDrawable(R.drawable.default_selector)
            this.getColor(R.color.textColor).let { it1 -> binding.expenseBtn.setTextColor(it1) }
            this.getColor(R.color.greenColor).let { it1 -> binding.incomeBtn.setTextColor(it1) }
            type = Constants.INCOME
        }
        else if (transactionModel.type == Constants.EXPENSE) {
            binding.incomeBtn.background = this.getDrawable(R.drawable.default_selector)
            binding.expenseBtn.background = this.getDrawable(R.drawable.expense_selector)
            this.getColor(R.color.textColor).let { it1 -> binding.incomeBtn.setTextColor(it1) }
            this.getColor(R.color.redColor).let { it1 -> binding.expenseBtn.setTextColor(it1) }
            type = Constants.EXPENSE
        }

        binding.date.setText(transactionModel.date.toString())

        binding.amount.setText(transactionModel.amount.toString())

        binding.category.setText(transactionModel.category)

        binding.transactionMethod.setText(transactionModel.account)

        binding.note.setText(transactionModel.note)

    }

    private fun updateExpense() {
        val expenseId = transactionModel.id
        val amount = binding.amount.text.toString().trim()
        val category = binding.category.text.toString().trim()
        val transactionMethod = binding.transactionMethod.text.toString()
        val note = binding.note.text.toString().trim()

        if (type == null) {
            type = transactionModel.type
        } else if (date == null) {
            date = transactionModel.date
        }

        if (amount.isEmpty() || category.isEmpty() || transactionMethod.isEmpty()) {
            Toast.makeText(this, "Please Fill All The Fields ", Toast.LENGTH_SHORT).show()
            return
        } else {
            val model = Transaction(
                type = type,
                category = category,
                account = transactionMethod,
                note = note,
                date = date,
                amount = amount.toDouble(),
                id = expenseId,
                uid = FirebaseAuth.getInstance().uid
            )
            if (expenseId != null) {
                FirebaseFirestore.getInstance().collection("transaction").document(expenseId)
                    .set(model)
            }

            if(isChecked=="category")
            {
                startActivity(Intent(this, AccountsActivity::class.java))
                finish()
            }
            else{
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }


        }
    }

}