package com.example.data.model

import java.util.UUID

data class StockRequest(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String = "",
    val itemSku: String = "",
    val itemModel: String = "",
    val itemName: String = "",
    val itemBrand: String = "",
    val itemCategory: String = "LED TV",
    val currentStock: Int = 0,
    val requestedQuantity: Int = 1,
    val urgency: String = "NORMAL", // NORMAL, HIGH, URGENT
    val note: String = "",
    val requestedByStaffId: String = "",
    val requestedByStaffName: String = "",
    val requestedByStaffRole: String = "SALES",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, FULFILLED
    val reviewNote: String = "",
    val reviewedByStaffName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isPending: Boolean
        get() = status.equals("PENDING", ignoreCase = true)

    val isApproved: Boolean
        get() = status.equals("APPROVED", ignoreCase = true) || status.equals("FULFILLED", ignoreCase = true)

    val isRejected: Boolean
        get() = status.equals("REJECTED", ignoreCase = true)
}
