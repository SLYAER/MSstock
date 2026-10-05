package com.example.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ElectronicsItem
import com.example.ui.theme.StockInStock
import com.example.ui.theme.StockLowStock
import com.example.ui.theme.StockOutOfStock
import java.text.NumberFormat
import java.util.Locale

// Data structure grouping a Brand's availability for a chosen Product and Size
data class BrandStockSummary(
    val brandName: String,
    val items: List<ElectronicsItem>,
    val totalStock: Int,
    val isOutOfStock: Boolean,
    val minPrice: Double,
    val maxPrice: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HierarchicalStockExplorerDialog(
    allItems: List<ElectronicsItem>,
    canViewCosts: Boolean,
    initialCategory: String = "LED TV",
    initialSize: String? = null,
    onDismiss: () -> Unit,
    onSellItem: (ElectronicsItem) -> Unit,
    onAdjustStock: (ElectronicsItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedSize by remember { mutableStateOf(initialSize ?: "55\"") }
    var expandedBrandName by remember { mutableStateOf<String?>(null) }

    // Dynamic sizes present in the catalog for the selected product category
    val availableSizes = remember(allItems, selectedCategory) {
        val defaultSizes = if (selectedCategory == "LED TV") {
            listOf("32\"", "43\"", "50\"", "55\"", "65\"", "75\"")
        } else emptyList()

        val catalogSizes = allItems
            .filter { it.category.equals(selectedCategory, ignoreCase = true) && it.size.isNotBlank() }
            .map { it.size }
            .distinct()

        val combined = (defaultSizes + catalogSizes).distinct().sorted()
        listOf("All Sizes") + combined
    }

    // Filter items matching the selected Category and Size
    val filteredCategoryItems = remember(allItems, selectedCategory, selectedSize) {
        allItems.filter { item ->
            val matchCat = item.category.equals(selectedCategory, ignoreCase = true)
            val matchSize = selectedSize == "All Sizes" || item.size.equals(selectedSize, ignoreCase = true)
            matchCat && matchSize
        }
    }

    // Known list of brands for the selected category (e.g. Haier, Onida, Sony, Samsung, LG, TCL)
    val brandSummaries = remember(allItems, selectedCategory, selectedSize) {
        // Guarantee popular brands for LED TV are always evaluated
        val defaultBrands = when (selectedCategory) {
            "LED TV" -> listOf("Haier", "Onida", "Sony", "Samsung", "LG", "TCL")
            "Smartphones" -> listOf("Apple", "Samsung", "Google", "Xiaomi", "OnePlus")
            else -> emptyList()
        }

        val catalogBrands = allItems
            .filter { it.category.equals(selectedCategory, ignoreCase = true) && it.brand.isNotBlank() }
            .map { it.brand }
            .distinct()

        val allCategoryBrands = (defaultBrands + catalogBrands).distinct().sorted()

        allCategoryBrands.map { brand ->
            val matchingBrandItems = filteredCategoryItems.filter { it.brand.equals(brand, ignoreCase = true) }
            val totalUnits = matchingBrandItems.sumOf { it.quantity }
            val isOutOfStock = totalUnits == 0
            val prices = matchingBrandItems.map { it.sellingPrice }
            val minP = prices.minOrNull() ?: 0.0
            val maxP = prices.maxOrNull() ?: 0.0

            BrandStockSummary(
                brandName = brand,
                items = matchingBrandItems,
                totalStock = totalUnits,
                isOutOfStock = isOutOfStock,
                minPrice = minP,
                maxPrice = maxP
            )
        }.sortedWith(compareBy({ it.isOutOfStock }, { it.brandName }))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Stock Availability Explorer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Filter: Product > Size > Brand > Model",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // STEP 1: Select Product Category
                    item {
                        Column {
                            Text(
                                text = "1. SELECT PRODUCT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("LED TV", "Smartphones", "Audio & Headphones", "Laptops & PCs").forEach { cat ->
                                    val isSelected = selectedCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedCategory = cat
                                            expandedBrandName = null
                                        },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (cat == "LED TV") {
                                                    Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(cat)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // STEP 2: Filter by Size
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedCategory == "LED TV") "2. SELECT SCREEN SIZE" else "2. SELECT SIZE / CAPACITY",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Active: $selectedSize",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableSizes.forEach { sz ->
                                    val isSelected = selectedSize == sz
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedSize = sz
                                            expandedBrandName = null
                                        },
                                        label = { Text(sz, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // STEP 3: Brand Availability Summary Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. BRANDS IN $selectedSize (${brandSummaries.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Out of stock brands are greyed out",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Brand Availability Cards
                    if (brandSummaries.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "No brand models registered for $selectedCategory in $selectedSize.",
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(brandSummaries, key = { it.brandName }) { summary ->
                            val isExpanded = expandedBrandName == summary.brandName
                            BrandAvailabilityCard(
                                summary = summary,
                                selectedSize = selectedSize,
                                isExpanded = isExpanded,
                                canViewCosts = canViewCosts,
                                onToggleExpand = {
                                    expandedBrandName = if (isExpanded) null else summary.brandName
                                },
                                onSellItem = onSellItem,
                                onAdjustStock = onAdjustStock
                            )
                        }
                    }
                }
            }
        }
    }
}

// Brand Availability Card with GREY OUT effect when Out of Stock!
@Composable
fun BrandAvailabilityCard(
    summary: BrandStockSummary,
    selectedSize: String,
    isExpanded: Boolean,
    canViewCosts: Boolean,
    onToggleExpand: () -> Unit,
    onSellItem: (ElectronicsItem) -> Unit,
    onAdjustStock: (ElectronicsItem) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val isOutOfStock = summary.isOutOfStock

    // Visual styles for Out of Stock vs In Stock
    val containerBgColor = if (isOutOfStock) {
        // Greyed out container
        Color(0xFFE2E8F0).copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isOutOfStock) {
        Color(0xFF64748B) // Slate grey text
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val brandInitialColor = if (isOutOfStock) {
        Color(0xFF94A3B8)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val avatarBg = if (isOutOfStock) {
        Color(0xFFCBD5E1)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "brand_chevron"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            .clickable(onClick = onToggleExpand)
            .testTag("brand_card_${summary.brandName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 0.dp else 2.dp),
        border = if (isOutOfStock) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Brand Initial Badge
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = avatarBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = summary.brandName.firstOrNull()?.uppercase() ?: "B",
                                fontWeight = FontWeight.Bold,
                                color = brandInitialColor,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = summary.brandName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            if (summary.items.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${summary.items.size} model${if (summary.items.size > 1) "s" else ""})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOutOfStock) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (summary.minPrice > 0.0) {
                            Text(
                                text = if (summary.minPrice == summary.maxPrice) currencyFormat.format(summary.minPrice)
                                else "${currencyFormat.format(summary.minPrice)} – ${currencyFormat.format(summary.maxPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOutOfStock) Color(0xFF94A3B8) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "No pricing registered",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Availability Status Chip + Animated Chevron
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOutOfStock) {
                        // Visibly Grey "Out of Stock" chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFCBD5E1)
                        ) {
                            Text(
                                text = "❌ Out of Stock (0)",
                                color = Color(0xFF475569),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    } else {
                        // Vibrant In Stock chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StockInStock.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "✅ ${summary.totalStock} in stock",
                                color = StockInStock,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation),
                        tint = if (isOutOfStock) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Click hint
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isOutOfStock) "Tap to view out-of-stock model details" else "Tap to view models & availability (${selectedSize})",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOutOfStock) Color(0xFF94A3B8) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }

            // Expanded Model Availability view with smooth animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(180)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(140)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = if (isOutOfStock) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MODELS AVAILABLE FOR ${summary.brandName.uppercase()} ($selectedSize):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (summary.items.isEmpty()) {
                        Text(
                            text = "No models currently in inventory for ${summary.brandName} in $selectedSize.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            summary.items.forEach { item ->
                                ModelAvailabilityItemCard(
                                    item = item,
                                    canViewCosts = canViewCosts,
                                    onSell = { onSellItem(item) },
                                    onAdjust = { onAdjustStock(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Model Availability Detail Card
@Composable
fun ModelAvailabilityItemCard(
    item: ElectronicsItem,
    canViewCosts: Boolean,
    onSell: () -> Unit,
    onAdjust: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val isOut = item.isOutOfStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOut) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Model: ${item.model.ifBlank { item.name }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOut) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "SKU: ${item.sku} • Size: ${item.size.ifBlank { "Standard" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock units badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOut) StockOutOfStock.copy(alpha = 0.15f) else StockInStock.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isOut) "0 in stock" else "${item.quantity} available",
                        color = if (isOut) StockOutOfStock else StockInStock,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing & Location Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currencyFormat.format(item.sellingPrice),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOut) Color(0xFF64748B) else MaterialTheme.colorScheme.primary
                    )
                    if (canViewCosts) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Cost: ${currencyFormat.format(item.costPrice)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(item.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSell,
                    enabled = item.quantity > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sell Unit", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onAdjust,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adjust", fontSize = 12.sp)
                }
            }
        }
    }
}
