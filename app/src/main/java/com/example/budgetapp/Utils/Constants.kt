package com.example.budgetapp.Utils

import com.example.budgetapp.R
import com.example.budgetapp.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object Constants {
    var INCOME = "INCOME"
    var EXPENSE = "EXPENSE"
    var categories: ArrayList<Category>? = null
    var income: Double = 0.0
    var expense: Double = 0.0

//    var DAILY = 0
//    var MONTHLY = 1
//    var CALENDAR = 2
//    var SUMMARY = 3
//    var NOTES = 4
//    var SELECTED_TAB = 0
//    var SELECTED_TAB_STATS = 0
//    var SELECTED_STATS_TYPE = INCOME

    fun setCategories() {
        categories = ArrayList()
        categories!!.add(Category("Salary", R.drawable.ic_salary, R.color.category1,10000))
        categories!!.add(Category("Business", R.drawable.ic_business, R.color.category2,1000))
        categories!!.add(Category("Investment", R.drawable.ic_investment, R.color.category3,1000))
        categories!!.add(Category("Loan", R.drawable.ic_loan, R.color.category4,10000))
        categories!!.add(Category("Rent", R.drawable.ic_rent, R.color.category5,10000))
        categories!!.add(Category("Other", R.drawable.ic_other, R.color.category6,1000))
    }

    fun accountCategories() {
        categories = ArrayList()
        categories!!.add(Category("Business", R.drawable.ic_business, R.color.category2,1000))
        categories!!.add(Category("Investment", R.drawable.ic_investment, R.color.category3,1000))
        categories!!.add(Category("Loan", R.drawable.ic_loan, R.color.category4,10000))
        categories!!.add(Category("Rent", R.drawable.ic_rent, R.color.category5,10000))
        categories!!.add(Category("Other", R.drawable.ic_other, R.color.category6,1000))
    }

    fun getCategoryDetails(categoryName: String?): Category? {
        for (cat in categories!!) {
            if (cat.categoryName.equals(categoryName)) {
                return cat
            }
        }
        return null
    }

    fun getAccountsColor(accountName: String?): Int {
        return when (accountName) {
            "Online" -> R.color.online_color
            "Cash" -> R.color.cash_color
            else -> R.color.default_color
        }
    }

    fun authentication(): Boolean {
        FirebaseFirestore.getInstance().collection("transaction")
            .whereEqualTo("uid", FirebaseAuth.getInstance().uid).get().addOnSuccessListener {
                if (it.documents.isEmpty()) false
            }
        return true
    }

}