package com.example.budgetapp.models

class Category {
    var categoryName: String? = null
    var categoryImage = 0
    var categoryColor = 0
    var limit:Long?=0

    constructor()
    constructor(categoryName: String?, categoryImage: Int, categoryColor: Int,limit : Long) {
        this.categoryName = categoryName
        this.categoryImage = categoryImage
        this.categoryColor = categoryColor
        this.limit=limit
    }
}