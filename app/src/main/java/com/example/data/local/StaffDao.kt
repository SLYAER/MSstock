package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members ORDER BY createdAt ASC")
    fun getAllStaff(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff_members WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: String): StaffEntity?

    @Query("SELECT * FROM staff_members WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getStaffByUsername(username: String): StaffEntity?

    @Query("SELECT COUNT(*) FROM staff_members")
    suspend fun getStaffCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaff(id: String)
}
