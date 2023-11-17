package com.example.budgetapp.UI.Activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.R
import com.example.budgetapp.UI.Fragments.AddTransactionFragment
import com.example.budgetapp.Utils.Constants
import com.example.budgetapp.adaptor.TransactionAdaptor
import com.example.budgetapp.databinding.ActivityCategoryBinding
import com.example.budgetapp.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.Calendar

@Suppress("DEPRECATION")
class CategoryActivity : AppCompatActivity()   {

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab)
    }
    private var isExpanded = false
    private lateinit var calendar: Calendar
    var category: String? = null
    private var called:Boolean=false
    private lateinit var transactionAdaptor: TransactionAdaptor
    private var expenseModel: Transaction? = null
    private var db = FirebaseFirestore.getInstance().collection("transaction")
    private lateinit var binding: ActivityCategoryBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        category = bundle?.get("category").toString()

        transactionAdaptor = TransactionAdaptor(this, object : TransactionAdaptor.TransactionClickListener {
                override fun onTransactionClick(transaction: Transaction?) {
                    choiceDialog(transaction)
                }
            })

        binding.transactionList.adapter = transactionAdaptor

        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = category.toString()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Constants.accountCategories()
        calendar = Calendar.getInstance()

        getData()

        if(called){
            totalBalance()
        }else{
            setData()
        }

        binding.apply {
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
                bundle.putString("key", "category")
                bundle.putString("type", Constants.EXPENSE)
                bottomSheetFragment.arguments = bundle
                bottomSheetFragment.show(supportFragmentManager, null)
                shrinkFab()
            }

            addIncome.setOnClickListener(){
                AddTransactionFragment().show(supportFragmentManager, null)
                shrinkFab()
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
            addBtn.visibility=View.VISIBLE
            addIncome.visibility=View.VISIBLE
            addText.visibility=View.VISIBLE
            addIncomeText.visibility=View.VISIBLE

            addBtn.startAnimation(fromBottomFabAnim)
            addIncome.startAnimation(fromBottomFabAnim)
            addText.startAnimation(fromBottomFabAnim)
            addIncomeText.startAnimation(fromBottomFabAnim)
        }

        isExpanded = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shrinkFab() {

        binding.apply {
            expandedButton.setImageResource(R.drawable.ic_uparrow);
            addBtn.visibility=View.GONE
            addText.visibility=View.GONE
            addIncome.visibility=View.GONE
            addIncomeText.visibility=View.GONE

            addBtn.startAnimation(toBottomFabAnim)
            addText.startAnimation(toBottomFabAnim)
            addIncome.startAnimation(toBottomFabAnim)
            addIncomeText.startAnimation(toBottomFabAnim)
        }

        isExpanded = false
    }

    private fun getData() {

        binding.progressBar.visibility=View.VISIBLE

        db.whereEqualTo("category", category).get().addOnSuccessListener {
            if (Constants.authentication()) {
                transactionAdaptor.clear()
                val dataList = it.documents
                for (ds in dataList) {
                    expenseModel = ds.toObject(Transaction::class.java)
                    transactionAdaptor.add(expenseModel)
                }
                binding.progressBar.visibility=View.GONE
            }
        }

    }

    private fun deleteDialog() {
        val bottomSheet = BottomSheetDialog(this@CategoryActivity)
        bottomSheet.setContentView(R.layout.delete_dialog)

        bottomSheet.findViewById<TextView>(R.id.textview_yes)?.setOnClickListener {
            expenseModel?.id.let {
                if (it != null) {
                    FirebaseFirestore.getInstance().collection("transaction").document(it).delete()
                }
            }
            finish()
            startActivity(Intent(this@CategoryActivity,AccountsActivity::class.java))
            bottomSheet.dismiss()
        }

        bottomSheet.findViewById<TextView>(R.id.textview_no)?.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun totalBalance() {

        FirebaseFirestore.getInstance().collection("transaction")
            .whereEqualTo("uid", FirebaseAuth.getInstance().uid)
            .get().addOnSuccessListener {
            val dataList = it.documents
            for (ds in dataList) {
                val model = ds.toObject(Transaction::class.java)
                if (model?.type == Constants.INCOME) {
                    Constants.income = Constants.income.plus(model.amount!!)
                } else {
                    if (model != null) {
                        Constants.expense = Constants.expense.plus(model.amount!!)
                    }
                }
            }
        }
        called=false
        setData()
        binding.progressBar.visibility=View.GONE
    }

    private fun setData() {

//        Log.e("@@@@@@","${Constants.income}+${Constants.expense}")

        var total : Long =0

        if(Constants.income.toInt() !=0 || Constants.expense.toInt() !=0){
            binding.emptyState.visibility= View.GONE

            binding.apply {
                incomeLbl.text = "Rs. ${Constants.income}"

                expenseLbl.text = "Rs. ${Constants.expense}"

                val total = Constants.income - Constants.expense

                if (total < 0) {
                    totalLbl.text = "Rs. $total"
                    totalLbl.setTextColor(this@CategoryActivity.getColor(R.color.redColor))
                } else {
                    totalLbl.text = "Rs. $total"
                    totalLbl.setTextColor(this@CategoryActivity.getColor(R.color.greenColor))
                }


            }

        } else {
            binding.apply {
                emptyState.visibility= View.VISIBLE
                totalLbl.text = total.toString()
                incomeLbl.text = Constants.income.toString()
                expenseLbl.text = Constants.expense.toString()
                totalLbl.text = total.toString()

            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onStop() {
//        super.onStop()
//        shrinkFab()
//    }

    private fun choiceDialog(transaction: Transaction?) {
        val bottomSheet = BottomSheetDialog(this@CategoryActivity)
        bottomSheet.setContentView(R.layout.choice_layout)

        bottomSheet.findViewById<TextView>(R.id.textview_Edit)?.setOnClickListener {
            val intent = Intent(this@CategoryActivity, Edit_Trasaction_Activity::class.java)
            intent.putExtra("model", transaction)
            intent.putExtra("key", "category")
            startActivity(intent)
            bottomSheet.dismiss()
        }

        bottomSheet.findViewById<TextView>(R.id.textview_delete)?.setOnClickListener {
            deleteDialog()
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

}