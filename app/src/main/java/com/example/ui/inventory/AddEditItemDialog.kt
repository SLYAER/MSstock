package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ElectronicsItem
import com.example.ui.theme.StockInStock
import com.example.ui.theme.StockLowStock
import com.example.ui.theme.StockOutOfStock
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

val ELECTRONICS_CATEGORIES = listOf(
    "LED TV",
    "Smartphones",
    "Laptops & PCs",
    "Audio & Headphones",
    "Components & Storage",
    "Accessories & Input",
    "Gaming & Displays",
    "Power & Batteries",
    "Wearables & IoT",
    "Other"
)

val POPULAR_LED_BRANDS = listOf("Haier", "Onida", "Sony", "Samsung", "LG", "TCL")
val COMMON_LED_SIZES = listOf("32\"", "43\"", "50\"", "55\"", "65\"", "75\"", "85\"")

val DEVICE_CONDITIONS = listOf(
    "NEW" to "Brand New",
    "REFURBISHED" to "Certified Refurbished",
    "OPEN_BOX" to "Open Box",
    "USED" to "Pre-Owned / Used"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    itemToEdit: ElectronicsItem? = null,
    onDismiss: () -> Unit,
    onSave: (ElectronicsItem, Boolean) -> Unit
) {
    val isEdit = itemToEdit != null

    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var sku by remember { mutableStateOf(itemToEdit?.sku ?: generateSampleSku("GEN")) }
    var category by remember { mutableStateOf(itemToEdit?.category ?: ELECTRONICS_CATEGORIES.first()) }
    var brand by remember { mutableStateOf(itemToEdit?.brand ?: "") }
    var size by remember { mutableStateOf(itemToEdit?.size ?: if (category == "LED TV") "55\"" else "") }
    var model by remember { mutableStateOf(itemToEdit?.model ?: "") }
    var quantityText by remember { mutableStateOf((itemToEdit?.quantity ?: 1).toString()) }
    var thresholdText by remember { mutableStateOf((itemToEdit?.minStockThreshold ?: 3).toString()) }
    var costPriceText by remember { mutableStateOf(if (isEdit) itemToEdit?.costPrice?.toString() ?: "" else "") }
    var sellingPriceText by remember { mutableStateOf(if (isEdit) itemToEdit?.sellingPrice?.toString() ?: "" else "") }
    var condition by remember { mutableStateOf(itemToEdit?.condition ?: "NEW") }
    var location by remember { mutableStateOf(itemToEdit?.location ?: "") }
    var warrantyText by remember { mutableStateOf((itemToEdit?.warrantyMonths ?: 12).toString()) }
    var notes by remember { mutableStateOf(itemToEdit?.notes ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var conditionDropdownExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var brandError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val cost = costPriceText.toDoubleOrNull() ?: 0.0
    val price = sellingPriceText.toDoubleOrNull() ?: 0.0
    val profit = price - cost
    val margin = if (price > 0.0) (profit / price) * 100.0 else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Edit Electronic Item" else "New Electronics Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // STEP 1: Category Dropdown
                    Column {
                        Text("1. PRODUCT CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                ELECTRONICS_CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // STEP 2: Brand
                    Column {
                        Text("2. BRAND / MAKER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (category == "LED TV" || category == "Gaming & Displays") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                POPULAR_LED_BRANDS.forEach { b ->
                                    FilterChip(
                                        selected = brand.equals(b, ignoreCase = true),
                                        onClick = {
                                            brand = b
                                            brandError = false
                                            if (name.isBlank() || name.contains("LED TV")) {
                                                name = "$b ${size.ifBlank { "55\"" }} LED TV ${if (model.isNotBlank()) "($model)" else ""}".trim()
                                            }
                                        },
                                        label = { Text(b) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedTextField(
                            value = brand,
                            onValueChange = {
                                brand = it
                                if (it.isNotBlank()) brandError = false
                            },
                            label = { Text("Brand Name *") },
                            placeholder = { Text("e.g. Haier, Onida, Sony, Samsung, LG") },
                            isError = brandError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_brand_input"),
                            singleLine = true
                        )
                    }

                    // STEP 3: Size
                    Column {
                        Text(if (category == "LED TV") "3. SCREEN SIZE" else "3. SIZE / CAPACITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (category == "LED TV" || category == "Gaming & Displays") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                COMMON_LED_SIZES.forEach { sz ->
                                    FilterChip(
                                        selected = size == sz,
                                        onClick = {
                                            size = sz
                                            if (name.isBlank() || name.contains("LED TV")) {
                                                name = "${brand.ifBlank { "Smart" }} $sz LED TV ${if (model.isNotBlank()) "($model)" else ""}".trim()
                                            }
                                        },
                                        label = { Text(sz) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedTextField(
                            value = size,
                            onValueChange = { size = it },
                            label = { Text(if (category == "LED TV") "Screen Size *" else "Size / Capacity") },
                            placeholder = { Text(if (category == "LED TV") "e.g. 55\", 43\"" else "e.g. 6.8\", 14\", 512GB") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // STEP 4: Model Number
                    Column {
                        Text("4. MODEL NUMBER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = model,
                            onValueChange = {
                                model = it
                                if (category == "LED TV" && (name.isBlank() || name.contains("LED TV"))) {
                                    name = "${brand.ifBlank { "Smart" }} ${size.ifBlank { "55\"" }} LED TV ($it)".trim()
                                }
                            },
                            label = { Text("Model Number *") },
                            placeholder = { Text("e.g. 55U6G, 55UIF, 43K6600, KD-55X74L") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_model_input"),
                            singleLine = true
                        )
                    }

                    // STEP 5: Product Display Title & SKU
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (it.isNotBlank()) nameError = false
                            },
                            label = { Text("Product Display Title *") },
                            placeholder = { Text("e.g. Haier 55\" 4K Smart LED TV") },
                            isError = nameError,
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("item_name_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU / Barcode") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { sku = generateSampleSku(category) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = "Generate SKU",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("item_sku_input"),
                            singleLine = true
                        )
                    }

                    // Stock Quantity & Low Threshold Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("In Stock Units *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("item_quantity_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = thresholdText,
                            onValueChange = { thresholdText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Low Stock Alert (Min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Cost Price & Selling Price Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = costPriceText,
                            onValueChange = {
                                costPriceText = it
                                priceError = false
                            },
                            label = { Text("Cost Price ($)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("item_cost_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = sellingPriceText,
                            onValueChange = {
                                sellingPriceText = it
                                priceError = false
                            },
                            label = { Text("Selling Price ($) *") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = priceError,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("item_price_input"),
                            singleLine = true
                        )
                    }

                    // Margin & Profit Preview Box
                    if (price > 0.0) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Gross Profit: $${String.format(Locale.US, "%.2f", profit)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Margin: ${String.format(Locale.US, "%.1f%%", margin)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (margin >= 25.0) StockInStock else if (margin >= 10.0) StockLowStock else StockOutOfStock
                                )
                            }
                        }
                    }

                    // Condition Dropdown & Location Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = conditionDropdownExpanded,
                            onExpandedChange = { conditionDropdownExpanded = !conditionDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = DEVICE_CONDITIONS.find { it.first == condition }?.second ?: condition,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Condition") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = conditionDropdownExpanded,
                                onDismissRequest = { conditionDropdownExpanded = false }
                            ) {
                                DEVICE_CONDITIONS.forEach { (code, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            condition = code
                                            conditionDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Shelf / Aisle Location") },
                            placeholder = { Text("Aisle 2 - Bin C") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Warranty & Notes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = warrantyText,
                            onValueChange = { warrantyText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Warranty (Months)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Supplier Info") },
                            placeholder = { Text("Serial, distributor batch, etc.") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = true
                                hasError = true
                            }
                            if (brand.isBlank()) {
                                brandError = true
                                hasError = true
                            }
                            val finalPrice = sellingPriceText.toDoubleOrNull()
                            if (finalPrice == null || finalPrice <= 0.0) {
                                priceError = true
                                hasError = true
                            }

                            if (!hasError) {
                                val item = ElectronicsItem(
                                    id = itemToEdit?.id ?: UUID.randomUUID().toString(),
                                    userId = itemToEdit?.userId ?: "",
                                    name = name.trim(),
                                    sku = sku.trim().ifBlank { generateSampleSku(category) },
                                    category = category,
                                    brand = brand.trim(),
                                    size = size.trim(),
                                    model = model.trim(),
                                    quantity = quantityText.toIntOrNull() ?: 0,
                                    minStockThreshold = thresholdText.toIntOrNull() ?: 3,
                                    costPrice = costPriceText.toDoubleOrNull() ?: 0.0,
                                    sellingPrice = finalPrice ?: 0.0,
                                    condition = condition,
                                    location = location.trim(),
                                    warrantyMonths = warrantyText.toIntOrNull() ?: 12,
                                    notes = notes.trim(),
                                    createdAt = itemToEdit?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(item, isEdit)
                            }
                        },
                        modifier = Modifier.testTag("save_item_button")
                    ) {
                        Text(if (isEdit) "Update Item" else "Save to Inventory")
                    }
                }
            }
        }
    }
}

private fun generateSampleSku(category: String): String {
    val prefix = when (category.lowercase()) {
        "smartphones" -> "PHN"
        "laptops & pcs" -> "LAP"
        "audio & headphones" -> "AUD"
        "components & storage" -> "CMP"
        "accessories & input" -> "ACC"
        "gaming & displays" -> "GAM"
        "power & batteries" -> "PWR"
        "wearables & iot" -> "IOT"
        else -> "ELC"
    }
    val code = Random.nextInt(1000, 9999)
    return "ELEC-$prefix-$code"
}
