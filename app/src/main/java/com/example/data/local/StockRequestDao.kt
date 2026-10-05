package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StockRequestDao {
    @Query("SELECT * FROM stock_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<StockRequestEntity>>

    @Query("SELECT * FROM stock_requests WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingRequests(): Flow<List<StockRequestEntity>>

    @Query("SELECT * FROM stock_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): StockRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: StockRequestEntity)

    @Update
    suspend fun updateRequest(request: StockRequestEntity)

    @Query("UPDATE stock_requests SET status = :status, reviewNote = :reviewNote, reviewedByStaffName = :reviewedBy, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, reviewNote: String, reviewedBy: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM stock_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)

    @Query("DELETE FROM stock_requests")
    suspend fun deleteAllRequests()

    @Query("SELECT COUNT(*) FROM stock_requests WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int
}
