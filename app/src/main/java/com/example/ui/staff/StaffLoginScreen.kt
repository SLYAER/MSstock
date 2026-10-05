package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StaffMember
import com.example.data.model.StaffRole
import com.example.ui.inventory.UiState
import com.example.ui.theme.StockInStock
import com.example.ui.theme.StockLowStock

@Composable
fun StaffLoginScreen(
    staffListState: UiState<List<StaffMember>>,
    onLogin: (StaffMember, String) -> Boolean,
    onRegisterProfile: (String, String, String, String) -> Unit,
    onInitializeOwner: () -> Unit
) {
    var selectedStaff by remember { mutableStateOf<StaffMember?>(null) }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Header
            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "MSstock Logo",
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MSstock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Electronics Store Staff & Role Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (staffListState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading team profiles...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                is UiState.Error -> {
                    Text(
                        text = "Unable to load staff profiles: ${staffListState.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                is UiState.Success -> {
                    val staffList = staffListState.data

                    if (staffList.isEmpty()) {
                        // Empty store: Prompt owner to initialize store profile
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Welcome to MSstock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Set up your initial Store Owner account. You can create or approve up to 20 staff profiles with customized roles (Sales, Owner, Manager, Clerk) and passwords.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onInitializeOwner,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("init_owner_button")
                                ) {
                                    Text("Initialize Store Owner: PARTH MEHTA (PIN: apple8901)")
                                }
                            }
                        }
                    } else {
                        // Main Login Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                if (selectedStaff == null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Select Your Profile",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${staffList.size} team member(s) registered",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Register button
                                        OutlinedButton(
                                            onClick = { showRegisterDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("register_profile_button")
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Set Up Profile", fontSize = 13.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(staffList, key = { it.id }) { staff ->
                                            StaffSelectableCard(
                                                staff = staff,
                                                onSelect = {
                                                    selectedStaff = staff
                                                    enteredPassword = ""
                                                    passwordError = null
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    val staff = selectedStaff!!

                                    // Selected staff profile view
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(46.dp),
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
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 18.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = staff.displayName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${staff.staffRole.badge} • @${staff.username}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        TextButton(onClick = { selectedStaff = null }) {
                                            Text("Change")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (staff.isPendingApproval) {
                                        // Profile has no password set yet by owner
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = StockLowStock.copy(alpha = 0.12f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.HourglassTop,
                                                    contentDescription = null,
                                                    tint = StockLowStock,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Awaiting Owner Password",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "The store owner must assign your password and role from the Owner dashboard before you can log in.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        OutlinedButton(
                                            onClick = { selectedStaff = null },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Back to Staff List")
                                        }
                                    } else {
                                        // Profile has a password -> enter password to unlock
                                        OutlinedTextField(
                                            value = enteredPassword,
                                            onValueChange = {
                                                enteredPassword = it
                                                passwordError = null
                                            },
                                            label = { Text("Enter Password / PIN") },
                                            placeholder = { Text("Password given by owner") },
                                            singleLine = true,
                                            isError = passwordError != null,
                                            supportingText = {
                                                val isParth = staff.displayName.contains("PARTH MEHTA", ignoreCase = true) ||
                                                    staff.username.equals("parth", ignoreCase = true)
                                                passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                                    ?: if (isParth) Text("Universal Owner password: apple8901", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                                    else null
                                            },
                                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            trailingIcon = {
                                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                    Icon(
                                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("staff_password_input")
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                if (enteredPassword.isBlank()) {
                                                    passwordError = "Please enter your password"
                                                    return@Button
                                                }
                                                val success = onLogin(staff, enteredPassword)
                                                if (!success) {
                                                    passwordError = "Incorrect password. Please verify with Store Owner."
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("unlock_app_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            val isParth = staff.displayName.contains("PARTH MEHTA", ignoreCase = true) ||
                                                staff.username.equals("parth", ignoreCase = true)
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (isParth) "Unlock as Universal Owner & Admin" else "Unlock MSstock (${staff.staffRole.label})")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer note
            Text(
                text = "MSstock • Electronics Store Inventory Management",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }

    // Dialog for team member self-registration
    if (showRegisterDialog) {
        RegisterProfileDialog(
            onDismiss = { showRegisterDialog = false },
            onSubmit = { name, username, contact, dept ->
                onRegisterProfile(name, username, contact, dept)
                showRegisterDialog = false
            }
        )
    }
}

@Composable
private fun StaffSelectableCard(
    staff: StaffMember,
    onSelect: () -> Unit
) {
    val isUniversalOwner = staff.displayName.contains("PARTH MEHTA", ignoreCase = true) ||
        staff.username.equals("parth", ignoreCase = true) ||
        staff.id == "owner_parth_mehta"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("staff_card_${staff.username}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUniversalOwner) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
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
                color = when {
                    isUniversalOwner -> MaterialTheme.colorScheme.primaryContainer
                    staff.staffRole == StaffRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                    staff.staffRole == StaffRole.SALES -> Color(0xFFE8F5E9)
                    staff.staffRole == StaffRole.MANAGER -> Color(0xFFFFF3E0)
                    else -> Color(0xFFE1F5FE)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isUniversalOwner) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Universal Owner",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = staff.displayName.firstOrNull()?.uppercase() ?: "S",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
                    if (isUniversalOwner) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "👑 UNIVERSAL ADMIN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (isUniversalOwner) "Universal Owner & Admin • @${staff.username}" else "${staff.staffRole.badge} • @${staff.username}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUniversalOwner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isUniversalOwner) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            if (staff.isPendingApproval) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StockLowStock.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⏳ Pending Password",
                        color = StockLowStock,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StockInStock.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Active",
                        color = StockInStock,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterProfileDialog(
    onDismiss: () -> Unit,
    onSubmit: (displayName: String, username: String, phoneOrEmail: String, department: String) -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneOrEmail by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Front Desk Sales") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Up Staff Profile")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Submit your details. The Store Owner will then assign your role (Sales, Clerk, etc.) and give you your login password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Alex Taylor") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_display_name_input")
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.filter { char -> char.isLetterOrDigit() || char == '_' } },
                    label = { Text("Username / Employee ID *") },
                    placeholder = { Text("e.g. alex_sales") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_username_input")
                )

                OutlinedTextField(
                    value = phoneOrEmail,
                    onValueChange = { phoneOrEmail = it },
                    label = { Text("Phone / Email") },
                    placeholder = { Text("e.g. +1 555-0192") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department / Assigned Section") },
                    placeholder = { Text("e.g. Smartphones & Audio Counter") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (displayName.isBlank()) {
                        errorText = "Please enter your full name"
                        return@Button
                    }
                    if (username.isBlank()) {
                        errorText = "Please choose a username"
                        return@Button
                    }
                    onSubmit(displayName, username, phoneOrEmail, department)
                },
                modifier = Modifier.testTag("submit_profile_button")
            ) {
                Text("Submit Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
