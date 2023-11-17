package com.example.budgetapp.UI.Activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.R
import com.example.budgetapp.Utils.Constants
import com.example.budgetapp.Utils.Helper
import com.example.budgetapp.adaptor.AccountActivityAdaptor
import com.example.budgetapp.databinding.ActivityAccountsBinding
import com.example.budgetapp.models.Transaction
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar

@Suppress("UNUSED_EXPRESSION", "DEPRECATION")
class AccountsActivity : AppCompatActivity() {

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab)
    }
    private var isExpanded = false
    private lateinit var binding: ActivityAccountsBinding
    private lateinit var calendar: Calendar
    private lateinit var accountActivityAdapter: AccountActivityAdaptor
    private var expenseModel: Transaction? = null
    private var db = FirebaseFirestore.getInstance().collection("transaction")
    private var transaction: HashMap<String, Double?> = HashMap()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = "Account"
        calendar = Calendar.getInstance()

        getData()
        updateDate()

        Constants.accountCategories()

        binding.apply {

            nextDateBtn.setOnClickListener {
                calendar.add(Calendar.MONTH, 1)
                updateDate()
                getData()

            }

            previousDateBtn.setOnClickListener {
                calendar.add(Calendar.MONTH, -1)
                updateDate()
                getData()

            }

            expandedButton.setOnClickListener {

                if (isExpanded) {
                    shrinkFab()
                } else {
                    expandFab()
                }
            }

//            addBtn.setOnClickListener {
//                val bottomSheetFragment = AddTransactionFragment()
//                val bundle = Bundle()
//                bundle.putString("key", "category")
//                bundle.putString("type", Constants.EXPENSE)
//                bottomSheetFragment.arguments = bundle
//                bottomSheetFragment.show(supportFragmentManager, null)
//                shrinkFab()
//            }
//
//            addIncome.setOnClickListener{
//                AddTransactionFragment().show(supportFragmentManager, null)
//                shrinkFab()
//            }

            transactionBtn.setOnClickListener {
                startActivity(Intent(this@AccountsActivity,MainActivity::class.java))
                shrinkFab()
            }

            statisticsBtn.setOnClickListener {
                startActivity(Intent(this@AccountsActivity,StatsActivity::class.java))
                shrinkFab()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun expandFab() {

        binding.apply {


            expandedButton.setImageResource(R.drawable.ic_downarrow)
            statisticsBtn.visibility=View.VISIBLE
            transactionBtn.visibility=View.VISIBLE
//            addBtn.visibility=View.VISIBLE
//            addIncome.visibility=View.VISIBLE
            statisticsText.visibility=View.VISIBLE
            transactionText.visibility=View.VISIBLE
//            addText.visibility=View.VISIBLE
//            addIncomeText.visibility=View.VISIBLE

            statisticsBtn.startAnimation(fromBottomFabAnim)
            transactionBtn.startAnimation(fromBottomFabAnim)
//            addBtn.startAnimation(fromBottomFabAnim)
//            addIncome.startAnimation(fromBottomFabAnim)
            statisticsText.startAnimation(fromBottomFabAnim)
            transactionText.startAnimation(fromBottomFabAnim)
//            addText.startAnimation(fromBottomFabAnim)
//            addIncomeText.startAnimation(fromBottomFabAnim)
        }

        isExpanded = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shrinkFab() {

        binding.apply {
            expandedButton.setImageResource(R.drawable.ic_uparrow);
            statisticsBtn.visibility = View.GONE
            transactionBtn.visibility = View.GONE
//            addBtn.visibility=View.GONE
//            addIncome.visibility=View.GONE
            statisticsText.visibility = View.GONE
            transactionText.visibility = View.GONE
//            addText.visibility=View.GONE
//            addIncomeText.visibility=View.GONE

            statisticsBtn.startAnimation(toBottomFabAnim)
            transactionBtn.startAnimation(toBottomFabAnim)
//            addBtn.startAnimation(toBottomFabAnim)
//            addIncome.startAnimation(toBottomFabAnim)
            statisticsText.startAnimation(toBottomFabAnim)
            transactionText.startAnimation(toBottomFabAnim)
//            addText.startAnimation(toBottomFabAnim)
//            addIncomeText.startAnimation(toBottomFabAnim)
        }

        isExpanded = false
    }

    private fun getData() {

        binding.progressBar.visibility=View.VISIBLE
        var business: Double? = 0.0
        var investment: Double? = 0.0
        var loan: Double? = 0.0
        var rent: Double? = 0.0
        var other: Double? = 0.0
        Constants.income = 0.0
        Constants.expense = 0.0

        calendar[Calendar.DAY_OF_MONTH] = 0

        val startTime = calendar.time


        calendar.add(Calendar.MONTH, 1)
        val endTime = calendar.time

        db.whereGreaterThanOrEqualTo("date", startTime).whereLessThan("date", endTime).get()
            .addOnSuccessListener {

                if(authentication())
                {
                    val dataList = it.documents
                    for (ds in dataList) {
                        expenseModel = ds.toObject(Transaction::class.java)

                        if (expenseModel?.type.equals("INCOME")) {
                            Constants.income += expenseModel?.amount!!
                        } else {
                            Constants.expense += expenseModel?.amount!!
                        }

                        when (expenseModel?.category) {
                            "Business" -> {
                                business = expenseModel?.amount?.let { it1 -> business?.plus(it1) }
                            }

                            "Investment" -> {
                                investment = expenseModel?.amount?.let { it1 -> investment?.plus(it1) }
                            }

                            "Loan" -> {
                                loan = expenseModel?.amount?.let { it1 -> loan?.plus(it1) }
                            }

                            "Rent" -> {
                                rent = expenseModel?.amount?.let { it1 -> rent?.plus(it1) }
                            }

                            "Other" -> {
                                other = expenseModel?.amount?.let { it1 -> other?.plus(it1) }
                            }
                        }
                    }

                    transaction["Business"] = business
                    transaction["Investment"] = investment
                    transaction["Loan"] = loan
                    transaction["Rent"] = rent
                    transaction["Other"] = other
                    accountActivityAdapter =
                        AccountActivityAdaptor(this, Constants.categories!!, transaction)
                    binding.categoryList.adapter = accountActivityAdapter
                    setData()
                    setGraph()
                    binding.progressBar.visibility=View.GONE
                }
            }


    }

    private fun updateDate() {
        binding.currentDate.text = Helper.formatDateByMonth(calendar.time)
        setData()
    }

    private fun setData() {

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING


        binding.apply {
            incomeLbl.text = "Rs. ${df.format(Constants.income)}"

            expenseLbl.text = "Rs. ${df.format(Constants.expense)}"

            val total = Constants.income - Constants.expense

            if (total < 0) {
                totalLbl.text = "Rs. ${df.format(total)}"
                totalLbl.setTextColor(this@AccountsActivity.getColor(R.color.redColor))
            } else {
                totalLbl.text = "Rs. ${df.format(total)}"
                totalLbl.setTextColor(this@AccountsActivity.getColor(R.color.greenColor))
            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        shrinkFab()
    }

    private fun authentication():Boolean {
        db.whereEqualTo("uid", FirebaseAuth.getInstance().uid).get()
            .addOnSuccessListener {
                if(it.documents.isEmpty())
                    false
            }
        return true
    }

    private fun setGraph() {
        val pieEntryList = ArrayList<PieEntry>()
        val colorList = ArrayList<Int>()
        if (Constants.income.toInt() != 0) {
            pieEntryList.add(PieEntry(Constants.income.toFloat(), "Income"))
            colorList.add(resources.getColor(R.color.greenColor))
        }
        if (Constants.expense.toInt() != 0) {
            pieEntryList.add(PieEntry(Constants.expense.toFloat(), "Expense"))
            colorList.add(resources.getColor(R.color.redColor))
        }

        if(Constants.income.toInt() != 0 || Constants.expense.toInt() !=0){
            binding.cardViewGraph.visibility=View.VISIBLE
            val pieDataSet = PieDataSet(pieEntryList, "${Constants.income - Constants.expense}")
            pieDataSet.colors=colorList
            pieDataSet.valueTextColor = resources.getColor(R.color.white)
            val pieData = PieData(pieDataSet)
            binding.graph.data = pieData
            binding.graph.invalidate()
        }else{
            binding.cardViewGraph.visibility=View.GONE
        }
    }
}