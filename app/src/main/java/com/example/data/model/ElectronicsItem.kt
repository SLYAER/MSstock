package com.example.data.model

data class ElectronicsItem(
    val id: String = "",
    val userId: String = "store_main",
    val name: String = "",
    val sku: String = "",
    val category: String = "LED TV",
    val brand: String = "",
    val size: String = "", // e.g. "32\"", "43\"", "55\"", "65\"", "75\""
    val model: String = "",
    val quantity: Int = 0,
    val minStockThreshold: Int = 5,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val condition: String = "NEW",
    val location: String = "",
    val warrantyMonths: Int = 12,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity in 1..minStockThreshold

    val isOutOfStock: Boolean
        get() = quantity <= 0

    val totalCostValue: Double
        get() = costPrice * quantity

    val totalRetailValue: Double
        get() = sellingPrice * quantity

    val profitMarginPercent: Double
        get() = if (sellingPrice > 0.0) ((sellingPrice - costPrice) / sellingPrice) * 100.0 else 0.0
}
