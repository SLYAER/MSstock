package com.example.ui.inventory

data class DailyRevenuePoint(
    val dateLabel: String,
    val shortLabel: String,
    val revenue: Double,
    val unitsSold: Int,
    val transactionCount: Int
)

data class WeeklyRevenuePoint(
    val weekLabel: String,
    val rangeLabel: String,
    val revenue: Double,
    val unitsSold: Int,
    val transactionCount: Int
)

data class SalesReportsData(
    val totalRevenue: Double = 0.0,
    val totalUnitsSold: Int = 0,
    val averageDailyRevenue: Double = 0.0,
    val averageWeeklyRevenue: Double = 0.0,
    val bestDayLabel: String = "Friday",
    val bestDayRevenue: Double = 0.0,
    val bestWeekLabel: String = "Current Week",
    val bestWeekRevenue: Double = 0.0,
    val topProductName: String = "Sony BRAVIA 55\" 4K Smart TV",
    val topProductRevenue: Double = 0.0,
    val dailyTrends: List<DailyRevenuePoint> = emptyList(),
    val weeklyTrends: List<WeeklyRevenuePoint> = emptyList()
)
