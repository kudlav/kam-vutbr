package com.kudlav.kam.data

import java.util.Date

data class Transaction(
    val time: Date?,
    val description: String,
    val amount: Double?
)
