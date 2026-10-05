package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.StockLog

@Entity(tableName = "stock_logs")
data class StockLogEntity(
    @PrimaryKey
    val id: String,
    val itemId: String,
    val itemName: String,
    val changeAmount: Int,
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String,
    val staffName: String = "",
    val staffRole: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): StockLog = StockLog(
        id = id,
        itemId = itemId,
        itemName = itemName,
        changeAmount = changeAmount,
        previousQuantity = previousQuantity,
        newQuantity = newQuantity,
        reason = reason,
        staffName = staffName,
        staffRole = staffRole,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(model: StockLog): StockLogEntity = StockLogEntity(
            id = model.id,
            itemId = model.itemId,
            itemName = model.itemName,
            changeAmount = model.changeAmount,
            previousQuantity = model.previousQuantity,
            newQuantity = model.newQuantity,
            reason = model.reason,
            staffName = model.staffName,
            staffRole = model.staffRole,
            createdAt = model.createdAt
        )
    }
}
