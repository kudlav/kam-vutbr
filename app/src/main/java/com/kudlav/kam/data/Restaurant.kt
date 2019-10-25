package com.kudlav.kam.data

data class Restaurant(
    val id: Int,
    var name: String,
    var address: String,
    var pictureUrl: String,
    var openingHours: String = "",
    var state: Char = '?',
    var favorite: Boolean = false
)
