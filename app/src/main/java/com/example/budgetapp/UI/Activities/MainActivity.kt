package com.example.budgetapp.UI.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.R
import com.example.budgetapp.UI.Fragments.AddTransactionFragment
import com.example.budgetapp.Utils.Constants
import com.example.budgetapp.Utils.Helper
import com.example.budgetapp.adaptor.TransactionAdaptor
import com.example.budgetapp.databinding.ActivityMainBinding
import com.example.budgetapp.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab)
    }
    private var isExpanded = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var calendar: Calendar
    private lateinit var transactionAdaptor: TransactionAdaptor
    private var expenseModel: Transaction? = null
    private var db = FirebaseFirestore.getInstance().collection("transaction")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        transactionAdaptor = TransactionAdaptor(this, object : TransactionAdaptor.TransactionClickListener {
                override fun onTransactionClick(transaction: Transaction?) {
                    choiceDialog(transaction)
                }
            })

        binding.transactionList.adapter = transactionAdaptor

        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = "Transaction"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Constants.setCategories()
        calendar = Calendar.getInstance()

        getData(calendar)
        updateDate()

        binding.apply {

            nextDateBtn.setOnClickListener {
                calendar.add(Calendar.MONTH, 1)
                updateDate()
                getData(calendar)
            }

            previousDateBtn.setOnClickListener {
                calendar.add(Calendar.MONTH, -1)
                updateDate()
                getData(calendar)
            }

            expandedButton.setOnClickListener {

                if (isExpanded) {
                    shrinkFab()
                } else {
                    expandFab()
                }
            }

            addBtn.setOnClickListener {
                val bottomSheetFragment = AddTransactionFragment()
                val bundle = Bundle()
                bundle.putString("type", Constants.EXPENSE)
                bottomSheetFragment.arguments = bundle
                bottomSheetFragment.show(supportFragmentManager, null)
                shrinkFab()
            }

            addIncome.setOnClickListener {
                AddTransactionFragment().show(supportFragmentManager, null)
                shrinkFab()
            }

            accountBtn.setOnClickListener {
                startActivity(Intent(this@MainActivity,AccountsActivity::class.java))
                finish()
            }

            statisticsBtn.setOnClickListener {
                startActivity(Intent(this@MainActivity,StatsActivity::class.java))
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun expandFab() {

        binding.apply {


            expandedButton.setImageResource(R.drawable.ic_downarrow)
            statisticsBtn.visibility=View.VISIBLE
            accountBtn.visibility=View.VISIBLE
            addBtn.visibility=View.VISIBLE
            addIncome.visibility=View.VISIBLE
            statisticsText.visibility=View.VISIBLE
            accountText.visibility=View.VISIBLE
            addText.visibility=View.VISIBLE
            addIncomeText.visibility=View.VISIBLE

            statisticsBtn.startAnimation(fromBottomFabAnim)
            accountBtn.startAnimation(fromBottomFabAnim)
            addBtn.startAnimation(fromBottomFabAnim)
            addIncome.startAnimation(fromBottomFabAnim)
            statisticsText.startAnimation(fromBottomFabAnim)
            accountText.startAnimation(fromBottomFabAnim)
            addText.startAnimation(fromBottomFabAnim)
            addIncomeText.startAnimation(fromBottomFabAnim)
        }

        isExpanded = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shrinkFab() {

        binding.apply {
            expandedButton.setImageResource(R.drawable.ic_uparrow);
            statisticsBtn.visibility = View.GONE
            accountBtn.visibility = View.GONE
            addBtn.visibility=View.GONE
            addIncome.visibility=View.GONE
            statisticsText.visibility = View.GONE
            accountText.visibility = View.GONE
            addText.visibility=View.GONE
            addIncomeText.visibility=View.GONE

            statisticsBtn.startAnimation(toBottomFabAnim)
            accountBtn.startAnimation(toBottomFabAnim)
            addBtn.startAnimation(toBottomFabAnim)
            addIncome.startAnimation(toBottomFabAnim)
            statisticsText.startAnimation(toBottomFabAnim)
            accountText.startAnimation(toBottomFabAnim)
            addText.startAnimation(toBottomFabAnim)
            addIncomeText.startAnimation(toBottomFabAnim)
        }

        isExpanded = false
    }

    private fun updateDate() {
        binding.currentDate.text = Helper.formatDateByMonth(calendar.time)
        setData()
    }

    override fun onResume() {
        Constants.income = 0.0
        Constants.expense = 0.0
        super.onResume()
    }

    private fun getData(calendar: Calendar) {
        binding.progressBar.visibility=View.VISIBLE

        Constants.income = 0.0
        Constants.expense = 0.0

        calendar[Calendar.DAY_OF_MONTH] = 0
        val startTime = calendar.time

        calendar.add(Calendar.MONTH, 1)
        val endTime = calendar.time


        db.whereGreaterThanOrEqualTo("date", startTime).whereLessThan("date", endTime).get()
            .addOnSuccessListener {
                if(Constants.authentication())
                {
                    transactionAdaptor.clear()
                    val dataList = it.documents
                    for (ds in dataList) {
                        expenseModel = ds.toObject(Transaction::class.java)
                        if (expenseModel?.type.equals("INCOME")) {
                            Constants.income += expenseModel?.amount!!
                        } else {
                            Constants.expense += expenseModel?.amount!!
                        }
                        transactionAdaptor.add(expenseModel)
                    }
                    setData()
                    binding.progressBar.visibility=View.GONE
                }
            }
    }

    private fun setData() {


        var total  =0.0

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING


        if(Constants.income.toInt() !=0 || Constants.expense.toInt() !=0){
            binding.emptyState.visibility=View.GONE

            binding.apply {
                incomeLbl.text = "Rs. ${df.format(Constants.income)}"

                expenseLbl.text = "Rs. ${df.format(Constants.expense)}"

                total = Constants.income - Constants.expense

                if (total < 0) {
                    totalLbl.text = "Rs. ${df.format(total)}"
                    totalLbl.setTextColor(this@MainActivity.getColor(R.color.redColor))
                } else {
                    totalLbl.text = "Rs. ${df.format(total)}"
                    totalLbl.setTextColor(this@MainActivity.getColor(R.color.greenColor))
                }


            }
        } else {
            binding.apply {
                emptyState.visibility=View.VISIBLE
                totalLbl.text = total.toString()
                incomeLbl.text = Constants.income.toString()
                expenseLbl.text = Constants.expense.toString()
                totalLbl.text = total.toString()

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        shrinkFab()
//        getData(calendar)
//        updateDate()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please")
        progressDialog.setMessage("Wait")
        progressDialog.setCancelable(false)
        if (FirebaseAuth.getInstance().currentUser == null) {
            progressDialog.show()
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
                progressDialog.cancel()
            }.addOnFailureListener {
                progressDialog.cancel()
                Toast.makeText(this@MainActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteDialog() {
        val bottomSheet = BottomSheetDialog(this@MainActivity)
        bottomSheet.setContentView(R.layout.delete_dialog)

        bottomSheet.findViewById<TextView>(R.id.textview_yes)?.setOnClickListener {
            expenseModel?.id.let {
                if (it != null) {
                    FirebaseFirestore.getInstance().collection("transaction").document(it).delete()
                }
            }
            finish()
            startActivity(this.intent)
            bottomSheet.dismiss()
        }

        bottomSheet.findViewById<TextView>(R.id.textview_no)?.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun choiceDialog(transaction: Transaction?) {
        val bottomSheet = BottomSheetDialog(this@MainActivity)
        bottomSheet.setContentView(R.layout.choice_layout)

        bottomSheet.findViewById<TextView>(R.id.textview_Edit)?.setOnClickListener {
            val intent = Intent(this@MainActivity, Edit_Trasaction_Activity::class.java)
            intent.putExtra("model", transaction)
            startActivity(intent)
            finish()
            bottomSheet.dismiss()
        }

        bottomSheet.findViewById<TextView>(R.id.textview_delete)?.setOnClickListener {
            deleteDialog()
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

}