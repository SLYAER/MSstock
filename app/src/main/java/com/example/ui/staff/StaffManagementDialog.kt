package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StaffMember
import com.example.data.model.StaffRole
import com.example.ui.inventory.UiState
import com.example.ui.theme.StockInStock
import com.example.ui.theme.StockLowStock
import com.example.ui.theme.StockOutOfStock
import java.util.UUID
import kotlin.random.Random

@Composable
fun StaffManagementDialog(
    staffListState: UiState<List<StaffMember>>,
    onSaveStaff: (StaffMember, Boolean) -> Unit,
    onDeleteStaff: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddMemberForm by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<StaffMember?>(null) }
    var memberToDelete by remember { mutableStateOf<StaffMember?>(null) }

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Team & Role Access",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Assign passwords & configure roles for your store staff",
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
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Add new staff member
                Button(
                    onClick = {
                        memberToEdit = null
                        showAddMemberForm = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_staff_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Team Member Directly")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Staff list
                when (staffListState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading team records...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    is UiState.Error -> {
                        Text(
                            text = "Error: ${staffListState.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    is UiState.Success -> {
                        val staffList = staffListState.data
                        val pendingList = staffList.filter { it.isPendingApproval }
                        val activeList = staffList.filter { !it.isPendingApproval }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .height(380.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (pendingList.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "⚠️ NEEDS PASSWORD & ROLE ASSIGNED (${pendingList.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StockLowStock,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(pendingList, key = { it.id }) { staff ->
                                    StaffMemberItemRow(
                                        staff = staff,
                                        onEdit = {
                                            memberToEdit = staff
                                            showAddMemberForm = true
                                        },
                                        onDelete = { memberToDelete = staff }
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ACTIVE TEAM MEMBERS (${activeList.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }

                            items(activeList, key = { it.id }) { staff ->
                                StaffMemberItemRow(
                                    staff = staff,
                                    onEdit = {
                                        memberToEdit = staff
                                        showAddMemberForm = true
                                    },
                                    onDelete = { memberToDelete = staff }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit staff member form dialog
    if (showAddMemberForm) {
        StaffFormDialog(
            existingMember = memberToEdit,
            onSave = { staff, isUpdate ->
                onSaveStaff(staff, isUpdate)
                showAddMemberForm = false
                memberToEdit = null
            },
            onDismiss = {
                showAddMemberForm = false
                memberToEdit = null
            }
        )
    }

    // Confirm deletion dialog
    memberToDelete?.let { staff ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Remove Staff Profile?") },
            text = { Text("Are you sure you want to remove ${staff.displayName} (@${staff.username})? They will no longer be able to log in to MSstock.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStaff(staff.id)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StaffMemberItemRow(
    staff: StaffMember,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (staff.isPendingApproval) StockLowStock.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = when (staff.staffRole) {
                    StaffRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                    StaffRole.SALES -> Color(0xFFE8F5E9)
                    StaffRole.MANAGER -> Color(0xFFFFF3E0)
                    StaffRole.CLERK -> Color(0xFFE1F5FE)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = staff.displayName.firstOrNull()?.uppercase() ?: "S",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = staff.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(@${staff.username})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isUniversalOwner = staff.displayName.contains("PARTH MEHTA", ignoreCase = true) ||
                    staff.username.equals("parth", ignoreCase = true) ||
                    staff.id == "owner_parth_mehta"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isUniversalOwner) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "👑 Universal Owner & Admin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = staff.staffRole.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (staff.pin.isNotBlank()) {
                        Text(
                            text = " • Password: ${staff.pin}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (staff.department.isNotBlank()) {
                    Text(
                        text = "Dept: ${staff.department}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (staff.canAccessHierarchy) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "✓ LED Filter Permitted",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "🔒 No LED Filter Perm",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Edit button (give password / change role)
            Button(
                onClick = onEdit,
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = if (staff.isPendingApproval) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else ButtonDefaults.filledTonalButtonColors()
            ) {
                Icon(
                    imageVector = if (staff.isPendingApproval) Icons.Default.Key else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (staff.isPendingApproval) "Give Password" else "Edit",
                    fontSize = 12.sp
                )
            }

            val isProtectedAdmin = staff.displayName.contains("PARTH MEHTA", ignoreCase = true) ||
                staff.username.equals("parth", ignoreCase = true) ||
                staff.id == "owner_parth_mehta" ||
                (staff.staffRole == StaffRole.OWNER && staff.username == "owner")

            if (!isProtectedAdmin) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Staff",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffFormDialog(
    existingMember: StaffMember?,
    onSave: (StaffMember, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isUpdate = existingMember != null
    var displayName by remember { mutableStateOf(existingMember?.displayName ?: "") }
    var username by remember { mutableStateOf(existingMember?.username ?: "") }
    var role by remember { mutableStateOf(existingMember?.role ?: StaffRole.SALES.name) }
    var pin by remember { mutableStateOf(existingMember?.pin ?: "") }
    var department by remember { mutableStateOf(existingMember?.department ?: "Sales Counter") }
    var phoneOrEmail by remember { mutableStateOf(existingMember?.phoneOrEmail ?: "") }
    var isActive by remember { mutableStateOf(existingMember?.isActive ?: true) }
    var hasHierarchyPermission by remember { mutableStateOf(existingMember?.hasHierarchyPermission ?: true) }
    var isPasswordVisible by remember { mutableStateOf(true) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isUpdate) Icons.Default.Edit else Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(if (isUpdate) "Configure Staff Profile & Role" else "Add Team Member")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name *") },
                    placeholder = { Text("e.g. Sarah Connor") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_name_input")
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.filter { char -> char.isLetterOrDigit() || char == '_' } },
                    label = { Text("Username *") },
                    placeholder = { Text("e.g. sarah_sales") },
                    singleLine = true,
                    enabled = !isUpdate || existingMember?.username != "owner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_username_input")
                )

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = when (role) {
                            "OWNER" -> "👑 Store Owner"
                            "MANAGER" -> "📦 Inventory Manager"
                            "SALES" -> "💼 Sales Representative"
                            "CLERK" -> "🛠️ Stock Clerk"
                            else -> role
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Role *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        StaffRole.entries.forEach { r ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(r.badge, fontWeight = FontWeight.Bold)
                                        Text(r.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    role = r.name
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Password / PIN Field (Owner assigns this!)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Assigned Password / PIN *") },
                    placeholder = { Text("Give them a password to log in") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        }
                    },
                    supportingText = {
                        Text("Tell the staff member this password so they can log in.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_pin_input")
                )

                // Quick Generate PIN button
                TextButton(
                    onClick = {
                        pin = Random.nextInt(1000, 9999).toString()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate 4-Digit PIN")
                }

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department / Section") },
                    placeholder = { Text("e.g. Front Counter, Warehouse") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Account Active")
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stock Hierarchy & Explorer",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Permission to use Brand > Size > Model filter & guided search",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasHierarchyPermission,
                        onCheckedChange = { hasHierarchyPermission = it }
                    )
                }

                formError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (displayName.isBlank()) {
                        formError = "Please enter display name"
                        return@Button
                    }
                    if (username.isBlank()) {
                        formError = "Please enter username"
                        return@Button
                    }
                    if (pin.isBlank()) {
                        formError = "Please enter a password / PIN for this user"
                        return@Button
                    }

                    val updatedStaff = StaffMember(
                        id = existingMember?.id ?: UUID.randomUUID().toString(),
                        username = username.trim().lowercase(),
                        displayName = displayName.trim(),
                        role = role,
                        pin = pin.trim(),
                        department = department.trim(),
                        phoneOrEmail = phoneOrEmail.trim(),
                        isActive = isActive,
                        hasHierarchyPermission = hasHierarchyPermission,
                        createdAt = existingMember?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updatedStaff, isUpdate)
                },
                modifier = Modifier.testTag("save_staff_submit")
            ) {
                Text(if (isUpdate) "Save Changes" else "Create Staff Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
