package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ElectronicsItem

@Entity(tableName = "electronics_items")
data class ElectronicsItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val brand: String,
    val size: String = "",
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
    fun toModel(): ElectronicsItem = ElectronicsItem(
        id = id,
        name = name,
        sku = sku,
        category = category,
        brand = brand,
        size = size,
        model = model,
        quantity = quantity,
        minStockThreshold = minStockThreshold,
        costPrice = costPrice,
        sellingPrice = sellingPrice,
        condition = condition,
        location = location,
        warrantyMonths = warrantyMonths,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: ElectronicsItem): ElectronicsItemEntity = ElectronicsItemEntity(
            id = model.id,
            name = model.name,
            sku = model.sku,
            category = model.category,
            brand = model.brand,
            size = model.size,
            model = model.model,
            quantity = model.quantity,
            minStockThreshold = model.minStockThreshold,
            costPrice = model.costPrice,
            sellingPrice = model.sellingPrice,
            condition = model.condition,
            location = model.location,
            warrantyMonths = model.warrantyMonths,
            notes = model.notes,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
