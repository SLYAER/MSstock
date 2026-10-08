package com.example.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StockLog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesReportsDialog(
    salesData: SalesReportsData,
    recentSalesLogs: List<StockLog>,
    onDismiss: () -> Unit
) {
    var isDailyTab by remember { mutableStateOf(true) }
    var selectedBarIndex by remember { mutableIntStateOf(-1) }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("sales_reports_modal"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sales Reports & Analytics",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Revenue trend chart visualization & store POS insights",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Tabs: Daily vs Weekly
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                isDailyTab = true
                                selectedBarIndex = -1
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDailyTab) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isDailyTab) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isDailyTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Revenue (7 Days)",
                                fontWeight = if (isDailyTab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isDailyTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                isDailyTab = false
                                selectedBarIndex = -1
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isDailyTab) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (!isDailyTab) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (!isDailyTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Weekly Revenue (4 Weeks)",
                                fontWeight = if (!isDailyTab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (!isDailyTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KPI Summary Metric Cards (4 Cards)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiMetricCard(
                        title = "TOTAL REVENUE",
                        value = currencyFormat.format(salesData.totalRevenue),
                        subtitle = "Gross Sales",
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = if (isDailyTab) "DAILY AVG" else "WEEKLY AVG",
                        value = currencyFormat.format(if (isDailyTab) salesData.averageDailyRevenue else salesData.averageWeeklyRevenue),
                        subtitle = "Per Period",
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "UNITS SOLD",
                        value = "${salesData.totalUnitsSold} units",
                        subtitle = "Physical Stock",
                        valueColor = Color(0xFF047857),
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "TOP PERFORMER",
                        value = salesData.topProductName,
                        subtitle = currencyFormat.format(salesData.topProductRevenue),
                        valueColor = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart Container Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isDailyTab) "Daily Revenue Trend (Past 7 Days)" else "Weekly Revenue Trend (Past 4 Weeks)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF2563EB), RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revenue ($)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Peak Period ⭐", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Interactive Canvas Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        ) {
                            if (isDailyTab) {
                                DailyRevenueCanvasChart(
                                    trends = salesData.dailyTrends,
                                    selectedIndex = selectedBarIndex,
                                    onSelectIndex = { selectedBarIndex = it }
                                )
                            } else {
                                WeeklyRevenueCanvasChart(
                                    trends = salesData.weeklyTrends,
                                    selectedIndex = selectedBarIndex,
                                    onSelectIndex = { selectedBarIndex = it }
                                )
                            }
                        }

                        // Selected Period Info Banner
                        if (selectedBarIndex >= 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isDailyTab && selectedBarIndex < salesData.dailyTrends.size) {
                                        val item = salesData.dailyTrends[selectedBarIndex]
                                        Text(
                                            text = "📅 ${item.dateLabel}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Revenue: ${currencyFormat.format(item.revenue)} • ${item.unitsSold} units (${item.transactionCount} orders)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    } else if (!isDailyTab && selectedBarIndex < salesData.weeklyTrends.size) {
                                        val item = salesData.weeklyTrends[selectedBarIndex]
                                        Text(
                                            text = "📊 ${item.weekLabel} (${item.rangeLabel})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Revenue: ${currencyFormat.format(item.revenue)} • ${item.unitsSold} units (${item.transactionCount} orders)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recent Transactions List
                Text(
                    text = "Recent POS Sales Transactions (${recentSalesLogs.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (recentSalesLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent sales recorded yet. Process sales from stock cards.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(recentSalesLogs.take(10), key = { it.id }) { log ->
                            RecentSaleRow(log = log)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Report")
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyRevenueCanvasChart(
    trends: List<DailyRevenuePoint>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    if (trends.isEmpty()) return

    val maxRevenue = trends.maxOfOrNull { it.revenue }?.coerceAtLeast(1000.0) ?: 1000.0
    val bestIndex = trends.indices.maxByOrNull { trends[it].revenue } ?: 0

    val primaryBarBrush = Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
    val bestBarBrush = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
    val selectedBarBrush = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(trends) {
                detectTapGestures { offset ->
                    val totalW = size.width
                    val padLeft = 60f
                    val padRight = 20f
                    val chartW = totalW - padLeft - padRight
                    val slotW = chartW / trends.size
                    val relX = offset.x - padLeft
                    if (relX >= 0 && relX < chartW) {
                        val index = (relX / slotW).toInt().coerceIn(0, trends.size - 1)
                        onSelectIndex(index)
                    }
                }
            }
    ) {
        val padLeft = 60f
        val padRight = 20f
        val padTop = 30f
        val padBottom = 40f

        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom
        val slotW = chartW / trends.size
        val barW = (slotW * 0.62f).coerceAtMost(46f)

        // Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        for (i in 0..4) {
            val ratio = i / 4f
            val y = padTop + chartH - (ratio * chartH)
            val valLabel = "$${(ratio * maxRevenue).toInt()}"

            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(padLeft, y),
                end = Offset(size.width - padRight, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                valLabel,
                padLeft - 10f,
                y + 8f,
                textPaint
            )
        }

        // Draw Bars & Labels
        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 26f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val starPaint = android.graphics.Paint().apply {
            color = Color(0xFF047857).toArgb()
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }

        trends.forEachIndexed { i, pt ->
            val h = ((pt.revenue / maxRevenue).toFloat() * chartH).coerceAtLeast(10f)
            val x = padLeft + (i * slotW) + (slotW - barW) / 2f
            val y = padTop + chartH - h

            val isBest = (i == bestIndex)
            val isSelected = (i == selectedIndex)

            val brush = when {
                isSelected -> selectedBarBrush
                isBest -> bestBarBrush
                else -> primaryBarBrush
            }

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barW, h),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Star indicator for best day
            if (isBest) {
                drawContext.canvas.nativeCanvas.drawText(
                    "⭐",
                    x + barW / 2f,
                    y - 12f,
                    starPaint
                )
            }

            // X-axis label
            labelPaint.isFakeBoldText = isBest || isSelected
            labelPaint.color = when {
                isSelected -> Color(0xFFD97706).toArgb()
                isBest -> Color(0xFF047857).toArgb()
                else -> Color(0xFF475569).toArgb()
            }

            drawContext.canvas.nativeCanvas.drawText(
                pt.shortLabel,
                x + barW / 2f,
                padTop + chartH + 28f,
                labelPaint
            )
        }
    }
}

@Composable
private fun WeeklyRevenueCanvasChart(
    trends: List<WeeklyRevenuePoint>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    if (trends.isEmpty()) return

    val maxRevenue = trends.maxOfOrNull { it.revenue }?.coerceAtLeast(1000.0) ?: 1000.0
    val bestIndex = trends.indices.maxByOrNull { trends[it].revenue } ?: 0

    val primaryBarBrush = Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
    val bestBarBrush = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
    val selectedBarBrush = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(trends) {
                detectTapGestures { offset ->
                    val totalW = size.width
                    val padLeft = 65f
                    val padRight = 20f
                    val chartW = totalW - padLeft - padRight
                    val slotW = chartW / trends.size
                    val relX = offset.x - padLeft
                    if (relX >= 0 && relX < chartW) {
                        val index = (relX / slotW).toInt().coerceIn(0, trends.size - 1)
                        onSelectIndex(index)
                    }
                }
            }
    ) {
        val padLeft = 65f
        val padRight = 20f
        val padTop = 30f
        val padBottom = 40f

        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom
        val slotW = chartW / trends.size
        val barW = (slotW * 0.58f).coerceAtMost(60f)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        for (i in 0..4) {
            val ratio = i / 4f
            val y = padTop + chartH - (ratio * chartH)
            val valLabel = "$${(ratio * maxRevenue).toInt()}"

            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(padLeft, y),
                end = Offset(size.width - padRight, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                valLabel,
                padLeft - 10f,
                y + 8f,
                textPaint
            )
        }

        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val starPaint = android.graphics.Paint().apply {
            color = Color(0xFF047857).toArgb()
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }

        trends.forEachIndexed { i, pt ->
            val h = ((pt.revenue / maxRevenue).toFloat() * chartH).coerceAtLeast(10f)
            val x = padLeft + (i * slotW) + (slotW - barW) / 2f
            val y = padTop + chartH - h

            val isBest = (i == bestIndex)
            val isSelected = (i == selectedIndex)

            val brush = when {
                isSelected -> selectedBarBrush
                isBest -> bestBarBrush
                else -> primaryBarBrush
            }

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barW, h),
                cornerRadius = CornerRadius(12f, 12f)
            )

            if (isBest) {
                drawContext.canvas.nativeCanvas.drawText(
                    "⭐ Best",
                    x + barW / 2f,
                    y - 12f,
                    starPaint
                )
            }

            labelPaint.isFakeBoldText = isBest || isSelected
            labelPaint.color = when {
                isSelected -> Color(0xFFD97706).toArgb()
                isBest -> Color(0xFF047857).toArgb()
                else -> Color(0xFF475569).toArgb()
            }

            drawContext.canvas.nativeCanvas.drawText(
                pt.weekLabel,
                x + barW / 2f,
                padTop + chartH + 28f,
                labelPaint
            )
        }
    }
}

@Composable
private fun RecentSaleRow(log: StockLog) {
    val dateStr = remember(log.createdAt) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(log.createdAt))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = log.itemName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${log.reason} • ${log.staffName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
