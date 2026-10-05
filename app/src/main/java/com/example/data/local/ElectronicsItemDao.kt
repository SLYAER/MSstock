package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectronicsItemDao {
    @Query("SELECT * FROM electronics_items ORDER BY updatedAt DESC")
    fun getAllItems(): Flow<List<ElectronicsItemEntity>>

    @Query("SELECT * FROM electronics_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): ElectronicsItemEntity?

    @Query("SELECT * FROM electronics_items WHERE LOWER(sku) = LOWER(:sku) LIMIT 1")
    suspend fun getItemBySku(sku: String): ElectronicsItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ElectronicsItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ElectronicsItemEntity>)

    @Update
    suspend fun updateItem(item: ElectronicsItemEntity)

    @Query("UPDATE electronics_items SET quantity = :newQuantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: String, newQuantity: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM electronics_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("SELECT COUNT(*) FROM electronics_items")
    suspend fun getItemCount(): Int
}
