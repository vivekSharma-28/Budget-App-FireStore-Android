package com.example.budgetapp.UI.Activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.R
import com.example.budgetapp.UI.Fragments.AddTransactionFragment
import com.example.budgetapp.Utils.Constants
import com.example.budgetapp.Utils.Helper
import com.example.budgetapp.databinding.ActivityStatsBinding
import com.example.budgetapp.models.Transaction
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.Calendar


class StatsActivity : AppCompatActivity() {

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab)
    }
    private var isExpanded = false
    private lateinit var binding: ActivityStatsBinding
    private lateinit var calendar: Calendar
    private var expenseModel: Transaction? = null
    private var category = ArrayList<Transaction>()
    private var db = FirebaseFirestore.getInstance().collection("transaction")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = "Statistics"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        calendar = Calendar.getInstance()

        updateDate()

        binding.apply {

            nextDateBtn.setOnClickListener {
                calendar.add(Calendar.MONTH, 1)
                updateDate()
            }

            previousDateBtn.setOnClickListener {
                calendar.add(Calendar.MONTH, -1)
                updateDate()
            }

            expandedButton.setOnClickListener {

                if (isExpanded) {
                    shrinkFab()
                } else {
                    expandFab()
                }
            }

            accountBtn.setOnClickListener {
                startActivity(Intent(this@StatsActivity,AccountsActivity::class.java))
                finish()
            }

            transactionBtn.setOnClickListener {
                startActivity(Intent(this@StatsActivity,MainActivity::class.java))
            }
        }

        binding.incomeBtn.setOnClickListener {
            binding.incomeBtn.background = this.getDrawable(R.drawable.income_selector)
            binding.expenseBtn.background = this.getDrawable(R.drawable.default_selector)
            binding.expenseBtn.setTextColor(this.getColor(R.color.textColor))
            binding.incomeBtn.setTextColor(this.getColor(R.color.greenColor))
            getData(calendar, Constants.INCOME)
        }

        binding.expenseBtn.setOnClickListener { view ->
            binding.incomeBtn.background = this.getDrawable(R.drawable.default_selector)
            binding.expenseBtn.background = this.getDrawable(R.drawable.expense_selector)
            binding.incomeBtn.setTextColor(this.getColor(R.color.textColor))
            binding.expenseBtn.setTextColor(this.getColor(R.color.redColor))
            getData(calendar, Constants.EXPENSE)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun expandFab() {

        binding.apply {


            expandedButton.setImageResource(R.drawable.ic_downarrow)
            transactionBtn.visibility=View.VISIBLE
            accountBtn.visibility=View.VISIBLE
            transactionText.visibility=View.VISIBLE
            accountText.visibility=View.VISIBLE
            /*addBtn.visibility=View.VISIBLE
            addText.visibility=View.VISIBLE*/

            transactionBtn.startAnimation(fromBottomFabAnim)
            accountBtn.startAnimation(fromBottomFabAnim)
            transactionText.startAnimation(fromBottomFabAnim)
            accountText.startAnimation(fromBottomFabAnim)
//            addBtn.startAnimation(fromBottomFabAnim)
//            addText.startAnimation(fromBottomFabAnim)
        }

        isExpanded = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shrinkFab() {

        binding.apply {
            expandedButton.setImageResource(R.drawable.ic_uparrow);
            transactionBtn.visibility = View.GONE
            accountBtn.visibility = View.GONE
            transactionText.visibility = View.GONE
            accountText.visibility = View.GONE
//            addBtn.visibility=View.GONE
//            addText.visibility=View.GONE

            transactionBtn.startAnimation(toBottomFabAnim)
            accountBtn.startAnimation(toBottomFabAnim)
            transactionText.startAnimation(toBottomFabAnim)
            accountText.startAnimation(toBottomFabAnim)
//            addBtn.startAnimation(toBottomFabAnim)
//            addText.startAnimation(toBottomFabAnim)
        }

        isExpanded = false
    }

    private fun updateDate() {
        binding.currentDate.text = Helper.formatDateByMonth(calendar.time)
        getData(calendar, Constants.INCOME)
        setIncomeButton()

    }

    private fun getData(calendar: Calendar, type: String) {
        binding.progressBar.visibility=View.VISIBLE
        category.clear()

        calendar[Calendar.DAY_OF_MONTH] = 0
        val startTime = calendar.time

        calendar.add(Calendar.MONTH, 1)
        val endTime = calendar.time

        db.whereGreaterThanOrEqualTo("date", startTime).whereLessThan("date", endTime).get()
            .addOnSuccessListener {
                if(Constants.authentication()){
                    val dataList = it.documents
                    for (ds in dataList) {
                        expenseModel = ds.toObject(Transaction::class.java)
                        if (expenseModel?.type.equals(type)) {
                            expenseModel?.let { it1 -> category.add(it1) }
                        }
                    }
                    setGraph()
                    binding.progressBar.visibility=View.GONE
                }
            }
    }

    private fun setGraph() {

        val pieEntryList = ArrayList<PieEntry>()
        for (i in 0..<category.size) {
            category[i].amount?.toFloat()?.let { PieEntry(it, category[i].category) }
                ?.let { pieEntryList.add(it) }
        }

        if (pieEntryList.isNotEmpty()) {
            binding.cardViewGraph.visibility = View.VISIBLE
            binding.emptyState.visibility=View.GONE
            val pieDataSet = PieDataSet(pieEntryList,"")
            pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toMutableList()
            pieDataSet.valueTextColor = resources.getColor(R.color.white)
            val pieData = PieData(pieDataSet)
            binding.graph.data = pieData
            binding.graph.invalidate()
        } else {
            binding.cardViewGraph.visibility = View.GONE
            binding.emptyState.visibility=View.VISIBLE
        }
    }

    private fun setIncomeButton(){
        binding.incomeBtn.background = this.getDrawable(R.drawable.income_selector)
        binding.expenseBtn.background = this.getDrawable(R.drawable.default_selector)
        binding.expenseBtn.setTextColor(this.getColor(R.color.textColor))
        binding.incomeBtn.setTextColor(this.getColor(R.color.greenColor))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}