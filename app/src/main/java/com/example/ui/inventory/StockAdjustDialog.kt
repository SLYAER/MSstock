package com.example.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ElectronicsItem
import com.example.ui.theme.StockInStock
import com.example.ui.theme.StockOutOfStock

val ADJUST_REASONS = listOf(
    "Supplier Delivery / Restock",
    "Customer Purchase / Sale",
    "Customer Return",
    "Damaged / Defective Unit",
    "Physical Inventory Audit",
    "Display / Demonstration Unit",
    "Other Adjustment"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustDialog(
    item: ElectronicsItem,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, reason: String) -> Unit
) {
    var delta by remember { mutableIntStateOf(1) }
    var selectedReason by remember { mutableStateOf(ADJUST_REASONS.first()) }
    var reasonDropdownExpanded by remember { mutableStateOf(false) }

    val newQuantity = (item.quantity + delta).coerceAtLeast(0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Adjust Stock",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current -> New Stock preview banner
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Current Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${item.quantity}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (delta >= 0) "+$delta" else "$delta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (delta >= 0) StockInStock else StockOutOfStock
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "New Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$newQuantity",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick preset delta chips
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-5, -1, 1, 5, 10).forEach { preset ->
                        FilterChip(
                            selected = delta == preset,
                            onClick = { delta = preset },
                            label = { Text(if (preset > 0) "+$preset" else "$preset") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom delta numeric field
                OutlinedTextField(
                    value = delta.toString(),
                    onValueChange = { input ->
                        val parsed = input.toIntOrNull()
                        if (parsed != null) delta = parsed
                        else if (input.isEmpty() || input == "-") {
                            // allow partial typing
                        }
                    },
                    label = { Text("Custom Adjustment (+/-)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Reason dropdown
                ExposedDropdownMenuBox(
                    expanded = reasonDropdownExpanded,
                    onExpandedChange = { reasonDropdownExpanded = !reasonDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reason for Change *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = reasonDropdownExpanded,
                        onDismissRequest = { reasonDropdownExpanded = false }
                    ) {
                        ADJUST_REASONS.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    selectedReason = r
                                    reasonDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { onConfirm(delta, selectedReason) },
                        enabled = delta != 0,
                        modifier = Modifier.testTag("confirm_adjust_stock_button")
                    ) {
                        Text("Update Stock")
                    }
                }
            }
        }
    }
}
