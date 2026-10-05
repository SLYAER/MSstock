package com.example.data.model

data class StockLog(
    val id: String = "",
    val userId: String = "store_main",
    val itemId: String = "",
    val itemName: String = "",
    val changeAmount: Int = 0,
    val previousQuantity: Int = 0,
    val newQuantity: Int = 0,
    val reason: String = "Manual Adjustment",
    val staffName: String = "",
    val staffRole: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
