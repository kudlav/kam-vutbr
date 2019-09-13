package com.kudlav.kam.data

data class Food(
    val type: FoodType,
    val weight: Int?,
    val nameCz: String,
    val nameEn: String,
    val allergens: ArrayList<Int>,
    val ingredients: List<String>,
    val priceStudent: Int?,
    val priceEmployee :Int?,
    val priceOther :Int?
)
