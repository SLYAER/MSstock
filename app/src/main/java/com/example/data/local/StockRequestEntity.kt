package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.StockRequest

@Entity(tableName = "stock_requests")
data class StockRequestEntity(
    @PrimaryKey
    val id: String,
    val itemId: String,
    val itemSku: String = "",
    val itemModel: String = "",
    val itemName: String,
    val itemBrand: String = "",
    val itemCategory: String = "LED TV",
    val currentStock: Int = 0,
    val requestedQuantity: Int = 1,
    val urgency: String = "NORMAL",
    val note: String = "",
    val requestedByStaffId: String = "",
    val requestedByStaffName: String = "",
    val requestedByStaffRole: String = "SALES",
    val status: String = "PENDING",
    val reviewNote: String = "",
    val reviewedByStaffName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): StockRequest = StockRequest(
        id = id,
        itemId = itemId,
        itemSku = itemSku,
        itemModel = itemModel,
        itemName = itemName,
        itemBrand = itemBrand,
        itemCategory = itemCategory,
        currentStock = currentStock,
        requestedQuantity = requestedQuantity,
        urgency = urgency,
        note = note,
        requestedByStaffId = requestedByStaffId,
        requestedByStaffName = requestedByStaffName,
        requestedByStaffRole = requestedByStaffRole,
        status = status,
        reviewNote = reviewNote,
        reviewedByStaffName = reviewedByStaffName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: StockRequest): StockRequestEntity = StockRequestEntity(
            id = model.id,
            itemId = model.itemId,
            itemSku = model.itemSku,
            itemModel = model.itemModel,
            itemName = model.itemName,
            itemBrand = model.itemBrand,
            itemCategory = model.itemCategory,
            currentStock = model.currentStock,
            requestedQuantity = model.requestedQuantity,
            urgency = model.urgency,
            note = model.note,
            requestedByStaffId = model.requestedByStaffId,
            requestedByStaffName = model.requestedByStaffName,
            requestedByStaffRole = model.requestedByStaffRole,
            status = model.status,
            reviewNote = model.reviewNote,
            reviewedByStaffName = model.reviewedByStaffName,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
