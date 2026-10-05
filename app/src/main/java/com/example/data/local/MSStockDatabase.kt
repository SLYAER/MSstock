package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StaffEntity::class,
        ElectronicsItemEntity::class,
        StockLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MSStockDatabase : RoomDatabase() {
    abstract fun staffDao(): StaffDao
    abstract fun electronicsItemDao(): ElectronicsItemDao
    abstract fun stockLogDao(): StockLogDao

    companion object {
        @Volatile
        private var INSTANCE: MSStockDatabase? = null

        fun getInstance(context: Context): MSStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MSStockDatabase::class.java,
                    "msstock_store.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
