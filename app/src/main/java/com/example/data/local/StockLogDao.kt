package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockLogDao {
    @Query("SELECT * FROM stock_logs ORDER BY createdAt DESC LIMIT 200")
    fun getAllLogs(): Flow<List<StockLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StockLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<StockLogEntity>)

    @Query("DELETE FROM stock_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM stock_logs")
    suspend fun deleteAllLogs()
}
