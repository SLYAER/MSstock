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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ElectronicsItem
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchAddModelDialog(
    isOwner: Boolean = false,
    onDismiss: () -> Unit,
    onSaveBatch: (List<ElectronicsItem>) -> Unit
) {
    var category by remember { mutableStateOf("LED TV") }
    var brand by remember { mutableStateOf("Haier") }
    var size by remember { mutableStateOf("55\"") }
    var initialQuantityText by remember { mutableStateOf("5") }
    var baseSellingPriceText by remember { mutableStateOf("499.99") }
    var baseCostPriceText by remember { mutableStateOf(if (isOwner) "380.00" else "0.0") }
    var location by remember { mutableStateOf("Warehouse Bay A") }

    var customModelInput by remember { mutableStateOf("") }
    val modelsList = remember {
        mutableStateListOf("55U6G", "55UIF", "55K6600", "55P735")
    }

    // Category presets
    val brandSuggestions = when (category) {
        "LED TV" -> listOf("Haier", "Onida", "Sony", "Samsung", "LG", "TCL", "Hisense")
        "Smartphones" -> listOf("Apple", "Samsung", "OnePlus", "Xiaomi", "Google", "Motorola")
        "Laptops & PCs" -> listOf("Apple", "Dell", "HP", "Lenovo", "Asus", "Acer", "MSI")
        "Audio & Headphones" -> listOf("Sony", "Bose", "JBL", "Sennheiser", "Apple", "Boat")
        else -> listOf("Sony", "Samsung", "Apple", "LG", "Logitech")
    }

    val modelSuggestions = when (category to brand) {
        "LED TV" to "Haier" -> listOf("55U6G", "43U6G", "65U6G", "32U6G", "55K6600")
        "LED TV" to "Onida" -> listOf("55UIF", "43UIF", "50UIF", "32HIF")
        "LED TV" to "Sony" -> listOf("BRAVIA-55X90L", "BRAVIA-65X90L", "BRAVIA-55A80L", "BRAVIA-43X75L")
        "LED TV" to "Samsung" -> listOf("UA55CU7700", "QA55QN90C", "UA43CU7700", "QA65QN90C")
        "LED TV" to "LG" -> listOf("OLED55C3", "55UQ7500", "65UQ8000", "OLED65G3")
        "LED TV" to "TCL" -> listOf("55P735", "65C745", "43P635", "55C845")
        "Smartphones" to "Apple" -> listOf("iPhone 15", "iPhone 15 Pro", "iPhone 15 Pro Max", "iPhone 14")
        "Smartphones" to "Samsung" -> listOf("Galaxy S24 Ultra", "Galaxy S24+", "Galaxy S24", "Galaxy A55")
        "Laptops & PCs" to "Apple" -> listOf("MacBook Air M3", "MacBook Pro 14 M3", "MacBook Pro 16 M3 Max")
        "Laptops & PCs" to "Dell" -> listOf("XPS 15 9530", "XPS 13 Plus", "Inspiron 16", "Alienware m16")
        else -> listOf("MOD-01", "MOD-02", "MOD-03", "PRO-MAX")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Quick Add Models (Batch)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Register multiple product models in one click",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Select Category
                    Text(
                        text = "1. Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ELECTRONICS_CATEGORIES.take(6).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = {
                                    category = cat
                                    if (cat == "LED TV") size = "55\"" else size = ""
                                },
                                label = { Text(cat) },
                                leadingIcon = if (category == cat) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    // 2. Select / Enter Brand
                    Text(
                        text = "2. Brand",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        brandSuggestions.forEach { b ->
                            Surface(
                                onClick = { brand = b },
                                shape = RoundedCornerShape(8.dp),
                                color = if (brand == b) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = b,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (brand == b) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Or Type Brand Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 3. Screen Size (for TVs)
                    if (category == "LED TV") {
                        Text(
                            text = "Screen Size",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            COMMON_LED_SIZES.forEach { s ->
                                Surface(
                                    onClick = { size = s },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (size == s) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = s,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (size == s) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Models to Add
                    Text(
                        text = "3. Model Numbers to Create (${modelsList.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Suggested model pills to quickly tap
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Quick Suggested $brand Models (Tap to add):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            modelSuggestions.forEach { m ->
                                val isAdded = modelsList.contains(m)
                                Surface(
                                    onClick = {
                                        if (!isAdded) modelsList.add(m)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isAdded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = m,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAdded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isAdded) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Custom model add input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customModelInput,
                            onValueChange = { customModelInput = it },
                            label = { Text("Add Model (e.g. 55U6G or 55UIF)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                val trimmed = customModelInput.trim()
                                if (trimmed.isNotBlank() && !modelsList.contains(trimmed)) {
                                    // Support comma separated
                                    trimmed.split(",", " ").filter { it.isNotBlank() }.forEach { mod ->
                                        if (!modelsList.contains(mod)) modelsList.add(mod)
                                    }
                                    customModelInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }

                    // Current models chips list
                    if (modelsList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            modelsList.forEach { m ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = m,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        IconButton(
                                            onClick = { modelsList.remove(m) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Default Pricing & Units
                    Text(
                        text = "4. Default Stock & Price Settings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = initialQuantityText,
                            onValueChange = { initialQuantityText = it },
                            label = { Text("Initial Units Each") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = baseSellingPriceText,
                            onValueChange = { baseSellingPriceText = it },
                            label = { Text("Selling Price ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (isOwner) {
                        OutlinedTextField(
                            value = baseCostPriceText,
                            onValueChange = { baseCostPriceText = it },
                            label = { Text("DP Price / Cost ($) [Owner Only]") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Warehouse / Shelf Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (brand.isBlank() || modelsList.isEmpty()) return@Button
                            val qty = initialQuantityText.toIntOrNull() ?: 5
                            val sellPrice = baseSellingPriceText.toDoubleOrNull() ?: 0.0
                            val costPrice = baseCostPriceText.toDoubleOrNull() ?: 0.0

                            val itemsToAdd = modelsList.map { modelNum ->
                                val cleanBrand = brand.trim()
                                val brandPrefix = cleanBrand.uppercase().take(3).padEnd(3, 'X')
                                val cleanSize = if (category == "LED TV" && size.isNotBlank()) " $size" else ""
                                val title = "$cleanBrand$cleanSize $modelNum $category".trim()

                                ElectronicsItem(
                                    id = UUID.randomUUID().toString(),
                                    name = title,
                                    sku = "ELEC-$brandPrefix-$modelNum-${UUID.randomUUID().toString().take(4).uppercase()}",
                                    category = category,
                                    brand = cleanBrand,
                                    size = size,
                                    model = modelNum,
                                    quantity = qty,
                                    minStockThreshold = 2,
                                    costPrice = if (isOwner) costPrice else 0.0,
                                    sellingPrice = sellPrice,
                                    condition = "NEW",
                                    location = location.trim(),
                                    warrantyMonths = 24,
                                    notes = "Batch registered"
                                )
                            }
                            onSaveBatch(itemsToAdd)
                        },
                        enabled = brand.isNotBlank() && modelsList.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_batch_models_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add ${modelsList.size} Models")
                    }
                }
            }
        }
    }
}
