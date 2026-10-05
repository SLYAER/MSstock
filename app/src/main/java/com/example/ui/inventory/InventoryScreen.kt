package com.example.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ElectronicsItem
import com.example.data.model.StaffMember
import com.example.data.model.StaffRole
import com.example.ui.staff.StaffManagementDialog
import com.example.ui.theme.StockInStock
import com.example.ui.theme.StockLowStock
import com.example.ui.theme.StockOutOfStock
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private val CATEGORY_FILTER_OPTIONS = listOf("All") + ELECTRONICS_CATEGORIES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onSignOut: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentStaff by viewModel.currentStaff.collectAsStateWithLifecycle()
    val rawItemsState by viewModel.rawItemsState.collectAsStateWithLifecycle()
    val stockLogsState by viewModel.stockLogsState.collectAsStateWithLifecycle()
    val staffListState by viewModel.staffListState.collectAsStateWithLifecycle()
    val stockRequestsState by viewModel.stockRequestsState.collectAsStateWithLifecycle()
    val pendingRequestsCount by viewModel.pendingRequestsCount.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val stockFilter by viewModel.stockFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val isOperationRunning by viewModel.isOperationRunning.collectAsStateWithLifecycle()
    val isDpUnlocked by com.example.data.util.SecurityGuard.isOwnerDpUnlocked.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showBatchModelDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemToAdjust by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemToSell by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemToDelete by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemForDetail by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemToRequestStock by remember { mutableStateOf<ElectronicsItem?>(null) }
    var showStockRequestsDialog by remember { mutableStateOf(false) }
    var showSecurityShieldDialog by remember { mutableStateOf(false) }
    var showLogsSheet by remember { mutableStateOf(false) }
    var showStaffDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    // Hierarchical Brand > Size > Model Guided Filter state & Permission
    var showHierarchyDialog by remember { mutableStateOf(false) }
    var showPermissionAlert by remember { mutableStateOf(false) }
    var hierarchyInitialCategory by remember { mutableStateOf("LED TV") }
    var hierarchyInitialSize by remember { mutableStateOf<String?>("55\"") }

    // Last completed sale receipt state
    var completedReceipt by remember { mutableStateOf<SaleReceipt?>(null) }

    val staff = currentStaff
    val staffRole = staff?.staffRole ?: StaffRole.SALES
    val canManageStaff = staff?.canManageStaff ?: false
    val canViewCosts = staff?.canViewCostsAndMargins ?: false
    val canEditItemDetails = staff?.canEditItemDetails ?: false
    val canDeleteItems = staff?.canDeleteItems ?: false
    val canAccessHierarchy = staff?.canAccessHierarchy ?: false

    // Pending staff count needing owner password
    val pendingStaffCount = remember(staffListState) {
        val st = staffListState
        if (st is UiState.Success) {
            st.data.count { it.isPendingApproval }
        } else 0
    }

    // Display user messages via Snackbar
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Sidebar Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "MSstock",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Store Operations Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Current Staff Profile Card (Password strictly hidden)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isParth = staff?.displayName?.contains("PARTH MEHTA", ignoreCase = true) == true
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape),
                            color = if (isParth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isParth) "👑" else (staff?.displayName?.take(1) ?: "S"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = staff?.displayName ?: "Staff Member",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isParth) "Universal Owner & Admin" else (staff?.staffRole?.badge ?: "Active Staff"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Section: INVENTORY & MODELS
                Text(
                    text = "INVENTORY & PRODUCTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                // Sidebar Option 1: Stock Catalog / Inventory
                NavigationDrawerItem(
                    label = { Text("Stock & Inventory") },
                    selected = selectedCategory == "All" && searchQuery.isBlank() && stockFilter == StockFilter.ALL,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.onSearchQueryChanged("")
                        viewModel.onCategorySelected("All")
                        viewModel.onStockFilterSelected(StockFilter.ALL)
                    },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    badge = {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "${stats.totalItems}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_stock")
                )

                // Sidebar Option 2: Add Product & Model
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text("Add Product / Model", fontWeight = FontWeight.Bold)
                            Text(
                                "Register individual product with SKU",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        itemToEdit = null
                        showAddDialog = true
                    },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_add_stock")
                )

                // Sidebar Option 3: Add Multiple Models (Batch)
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text("Add Multiple Models (Batch)", fontWeight = FontWeight.Bold)
                            Text(
                                "Fast-add TV, Mobile & PC models",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showBatchModelDialog = true
                    },
                    icon = { Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFD97706)) },
                    badge = {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Quick",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_batch_models")
                )

                // Sidebar Option 4: Guided LED TV Explorer
                NavigationDrawerItem(
                    label = { Text("LED TV Guided Filter") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (canAccessHierarchy) {
                            hierarchyInitialCategory = "LED TV"
                            hierarchyInitialSize = "55\""
                            showHierarchyDialog = true
                        } else {
                            showPermissionAlert = true
                        }
                    },
                    icon = { Icon(Icons.Default.Tv, contentDescription = null) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_led_explorer")
                )

                // Sidebar Option 5: Stock Requests (Sales Requisitions & Approvals)
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text("Stock Requests")
                            Text(
                                if (canViewCosts) "Fulfill & approve restocks" else "Request low stock models",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showStockRequestsDialog = true
                    },
                    icon = {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (pendingRequestsCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    badge = {
                        if (pendingRequestsCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("$pendingRequestsCount")
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_stock_requests")
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Section: MANAGEMENT & AUDIT
                Text(
                    text = "MANAGEMENT & AUDIT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                // Sidebar Option 6: Manage Employees (Universal Owner & Admin)
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text("Manage Employees")
                            Text(
                                if (canManageStaff) "Team roles & approvals" else "Owner & Admin access only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (canManageStaff) {
                            showStaffDialog = true
                        } else {
                            showPermissionAlert = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.ManageAccounts,
                            contentDescription = null,
                            tint = if (canManageStaff) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    badge = {
                        if (pendingStaffCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("$pendingStaffCount")
                            }
                        } else if (staff?.staffRole == StaffRole.OWNER) {
                            Text(
                                "Admin",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_employees")
                )

                // Sidebar Option 7: Stock Logs & Audit Trail
                NavigationDrawerItem(
                    label = { Text("Stock Logs & Audit Trail") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showLogsSheet = true
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_audit_logs")
                )

                // Sidebar Option 7b: Security & Protection Guard
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text("Security & Protection Guard")
                            Text(
                                "Zero-trust DP privacy & defenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSecurityShieldDialog = true
                    },
                    icon = {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF0F766E)
                        )
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("sidebar_nav_security")
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Sidebar Option 9: Switch Staff / Sign Out
                NavigationDrawerItem(
                    label = { Text("Switch Staff / Sign Out") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSignOutConfirm = true
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(bottom = 16.dp)
                        .testTag("sidebar_nav_signout")
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("sidebar_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Sidebar Navigation Menu"
                            )
                        }
                    },
                    title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MSstock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = staff?.let { "${it.staffRole.badge} • ${it.displayName}" } ?: "Electronics Store",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Seed catalog button (Owner / Manager)
                    if (canViewCosts) {
                        IconButton(
                            onClick = { viewModel.seedSampleCatalog() },
                            enabled = !isOperationRunning
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dataset,
                                contentDescription = "Seed Sample Catalog",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Staff Management button (Owner only)
                    if (canManageStaff) {
                        IconButton(onClick = { showStaffDialog = true }) {
                            if (pendingStaffCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("$pendingStaffCount")
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = "Staff Management",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Staff Management",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Stock Requests button with badge
                    IconButton(onClick = { showStockRequestsDialog = true }) {
                        if (pendingRequestsCount > 0) {
                            BadgedBox(badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text("$pendingRequestsCount")
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Stock Requests",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Stock Requests",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stock Logs button
                    IconButton(onClick = { showLogsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Audit Activity Log",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Security & Protection Guard button
                    IconButton(onClick = { showSecurityShieldDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security & Protection Guard",
                            tint = Color(0xFF0F766E)
                        )
                    }

                    // Switch Staff / Lock Button
                    IconButton(onClick = { showSignOutConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock / Switch Staff",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (canEditItemDetails) {
                FloatingActionButton(
                    onClick = {
                        itemToEdit = null
                        showAddDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_item_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Electronics Product")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Staff Active Role Banner
            item {
                StaffRoleBanner(staff = staff)
            }

            // Stats Dashboard
            item {
                InventoryDashboardCard(
                    stats = stats,
                    canViewCosts = canViewCosts
                )
            }

            // Live Stock Alert Ticker Banner
            item {
                LiveStockAlertBanner(
                    stats = stats,
                    onFilterLowStock = { viewModel.onStockFilterSelected(StockFilter.LOW_STOCK) },
                    onFilterOutOfStock = { viewModel.onStockFilterSelected(StockFilter.OUT_OF_STOCK) }
                )
            }

            // Enhanced Search Bar & Direct Model Search Section with Model Auto-Fill Suggestions
            item {
                val allItems = (rawItemsState as? UiState.Success<List<ElectronicsItem>>)?.data ?: emptyList()
                SearchBarSection(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    sortOption = sortOption,
                    onSortOptionSelected = viewModel::onSortOptionSelected,
                    showSortMenu = showSortMenu,
                    onToggleSortMenu = { showSortMenu = !showSortMenu },
                    allItems = allItems,
                    onQuickRegisterModel = { modelNum ->
                        itemToEdit = ElectronicsItem(
                            category = "LED TV",
                            model = modelNum
                        )
                        showAddDialog = true
                    },
                    onOpenHierarchyFilter = { cat, size ->
                        if (canAccessHierarchy) {
                            hierarchyInitialCategory = cat
                            hierarchyInitialSize = size
                            showHierarchyDialog = true
                        } else {
                            showPermissionAlert = true
                        }
                    }
                )
            }

            // Category Chips Carousel
            item {
                CategoryChipsRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected
                )
            }

            // Stock Status Filter Chips
            item {
                StockFilterChipsRow(
                    selectedFilter = stockFilter,
                    onFilterSelected = viewModel::onStockFilterSelected
                )
            }

            // Inventory List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Products (${filteredItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isOperationRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                }
            }

            // Empty state or Items List
            when (rawItemsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Error Loading Inventory",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = (rawItemsState as UiState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    if (filteredItems.isEmpty()) {
                        item {
                            EmptyInventoryPlaceholder(
                                isCatalogEmpty = (rawItemsState as UiState.Success<List<ElectronicsItem>>).data.isEmpty(),
                                onAddProduct = {
                                    itemToEdit = null
                                    showAddDialog = true
                                },
                                onAddBatch = {
                                    showBatchModelDialog = true
                                },
                                onSeedSample = { viewModel.seedSampleCatalog() }
                            )
                        }
                    } else {
                        items(filteredItems, key = { it.id }) { item ->
                            ElectronicsItemCard(
                                item = item,
                                canViewCosts = canViewCosts,
                                isDpUnlocked = isDpUnlocked,
                                onUnlockDp = { showSecurityShieldDialog = true },
                                canEdit = canEditItemDetails,
                                canDelete = canDeleteItems,
                                onViewDetail = { itemForDetail = item },
                                onSell = { itemToSell = item },
                                onAdjust = { itemToAdjust = item },
                                onEdit = {
                                    itemToEdit = item
                                    showAddDialog = true
                                },
                                onDelete = { itemToDelete = item },
                                onRequestStock = { itemToRequestStock = item }
                            )
                        }
                    }
                }
            }
        }
    }
    }

    // Product Detail & Specifications Sheet Dialog
    itemForDetail?.let { item ->
        ProductDetailDialog(
            item = item,
            canViewCosts = canViewCosts,
            onDismiss = { itemForDetail = null },
            onSell = {
                itemForDetail = null
                itemToSell = item
            },
            onAdjust = {
                itemForDetail = null
                itemToAdjust = item
            },
            onEdit = {
                itemForDetail = null
                itemToEdit = item
                showAddDialog = true
            },
            onRequestStock = {
                val target = itemForDetail
                itemForDetail = null
                itemToRequestStock = target
            }
        )
    }

    // Hierarchical Stock Availability Explorer Dialog (Brand > Size > Model with greyed out out-of-stock brands)
    if (showHierarchyDialog) {
        val itemsList = (rawItemsState as? UiState.Success<List<ElectronicsItem>>)?.data ?: emptyList()
        HierarchicalStockExplorerDialog(
            allItems = itemsList,
            canViewCosts = canViewCosts,
            initialCategory = hierarchyInitialCategory,
            initialSize = hierarchyInitialSize,
            onDismiss = { showHierarchyDialog = false },
            onSellItem = { item ->
                showHierarchyDialog = false
                itemToSell = item
            },
            onAdjustStock = { item ->
                showHierarchyDialog = false
                itemToAdjust = item
            }
        )
    }

    // Permission Denied Dialog
    if (showPermissionAlert) {
        AlertDialog(
            onDismissRequest = { showPermissionAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Permission Required")
                }
            },
            text = {
                Text(
                    text = "The Store Owner has not granted your profile permission to access the Hierarchical Stock Explorer & Product Finder.\n\nPlease ask the store owner to enable 'Stock Hierarchy & Explorer' for your account in the Team Management dashboard.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showPermissionAlert = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // Quick Sale / POS Dialog
    itemToSell?.let { item ->
        QuickSaleDialog(
            item = item,
            onDismiss = { itemToSell = null },
            onConfirmSale = { qtySold, paymentMethod, customerName ->
                viewModel.processSale(
                    item = item,
                    quantitySold = qtySold,
                    paymentMethod = paymentMethod,
                    customerName = customerName,
                    onComplete = { totalSaleAmount ->
                        completedReceipt = SaleReceipt(
                            receiptNumber = "RCP-${Random.nextInt(100000, 999999)}",
                            itemName = item.name,
                            sku = item.sku,
                            quantity = qtySold,
                            unitPrice = item.sellingPrice,
                            totalAmount = totalSaleAmount,
                            paymentMethod = paymentMethod,
                            customerName = customerName,
                            salesperson = staff?.displayName ?: "Store Cashier",
                            date = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())
                        )
                        itemToSell = null
                    }
                )
            }
        )
    }

    // Sale Receipt Dialog
    completedReceipt?.let { receipt ->
        ReceiptDialog(
            receipt = receipt,
            onDismiss = { completedReceipt = null }
        )
    }

    // Quick Stock Adjustment Dialog
    itemToAdjust?.let { item ->
        StockAdjustDialog(
            item = item,
            onDismiss = { itemToAdjust = null },
            onConfirm = { delta, reason ->
                viewModel.quickAdjustStock(item, delta, reason)
                itemToAdjust = null
            }
        )
    }

    // Add / Edit Product Dialog
    if (showAddDialog) {
        AddEditItemDialog(
            itemToEdit = itemToEdit,
            isOwner = canViewCosts,
            onDismiss = {
                showAddDialog = false
                itemToEdit = null
            },
            onSave = { item, isUpdate ->
                viewModel.saveItem(item, isUpdate) {
                    showAddDialog = false
                    itemToEdit = null
                }
            }
        )
    }

    // Batch Add Multiple Models Dialog
    if (showBatchModelDialog) {
        BatchAddModelDialog(
            isOwner = canViewCosts,
            onDismiss = { showBatchModelDialog = false },
            onSaveBatch = { items ->
                viewModel.saveItemsBatch(items) {
                    showBatchModelDialog = false
                }
            }
        )
    }

    // Request Stock Dialog (Sales staff & low-stock model restock requests)
    itemToRequestStock?.let { item ->
        RequestStockDialog(
            item = item,
            currentStaff = staff,
            onDismiss = { itemToRequestStock = null },
            onSubmitRequest = { quantity, urgency, note ->
                viewModel.requestStock(item, quantity, urgency, note)
                itemToRequestStock = null
            }
        )
    }

    // Stock Requests Management Dialog (Owner approvals & sales tracking)
    if (showStockRequestsDialog) {
        val requests = (stockRequestsState as? UiState.Success<List<com.example.data.model.StockRequest>>)?.data ?: emptyList()
        StockRequestsDialog(
            requests = requests,
            currentStaff = staff,
            onDismiss = { showStockRequestsDialog = false },
            onApprove = { requestId, addStock, note ->
                viewModel.approveStockRequest(requestId, addStock, note)
            },
            onReject = { requestId, reason ->
                viewModel.rejectStockRequest(requestId, reason)
            },
            onDelete = { requestId ->
                viewModel.deleteStockRequest(requestId)
            }
        )
    }

    // Delete confirmation
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove '${item.name}' (${item.sku}) from the store catalog?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Stock Activity Log Bottom Sheet
    if (showLogsSheet) {
        StockLogsBottomSheet(
            logsState = stockLogsState,
            onDismiss = { showLogsSheet = false }
        )
    }

    // Staff Management Dialog (Owner only)
    if (showStaffDialog) {
        StaffManagementDialog(
            staffListState = staffListState,
            onSaveStaff = { member, isUpdate ->
                viewModel.saveStaffMember(member, isUpdate)
            },
            onDeleteStaff = { staffId ->
                viewModel.deleteStaffMember(staffId)
            },
            onDismiss = { showStaffDialog = false }
        )
    }

    // Security & Protection Guard Dialog
    if (showSecurityShieldDialog) {
        SecurityShieldDialog(
            isOwnerRole = canViewCosts,
            currentStaffPin = staff?.pin,
            onDismiss = { showSecurityShieldDialog = false }
        )
    }

    // Sign out confirmation
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Switch Staff / Lock") },
            text = { Text("Lock MSstock and return to the Staff Access Portal?") },
            confirmButton = {
                Button(onClick = {
                    showSignOutConfirm = false
                    viewModel.logoutStaff()
                    onSignOut()
                }) {
                    Text("Lock & Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StaffRoleBanner(staff: StaffMember?) {
    val role = staff?.staffRole ?: StaffRole.SALES
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (role) {
                StaffRole.OWNER -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                StaffRole.SALES -> Color(0xFFE8F5E9)
                StaffRole.MANAGER -> Color(0xFFFFF3E0)
                StaffRole.CLERK -> Color(0xFFE1F5FE)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = role.badge,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logged in as ${staff?.displayName ?: "Staff"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (role == StaffRole.SALES) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "POS Mode Active",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryDashboardCard(
    stats: InventoryStats,
    canViewCosts: Boolean
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Store Inventory Overview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (stats.lowStockCount > 0) {
                    Surface(
                        color = StockLowStock.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${stats.lowStockCount} Low Stock Alert",
                            color = StockLowStock,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(title = "Products", value = "${stats.totalItems}")
                MetricColumn(title = "Units in Stock", value = "${stats.totalUnits}")
                MetricColumn(title = "Retail Value", value = currencyFormat.format(stats.totalRetailValue))
            }

            // Financial breakdown (Only for Owner and Manager roles!)
            if (canViewCosts) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricColumn(
                        title = "Wholesale Cost",
                        value = currencyFormat.format(stats.totalCostValue)
                    )
                    MetricColumn(
                        title = "Expected Profit",
                        value = currencyFormat.format(stats.potentialProfit)
                    )
                    MetricColumn(
                        title = "Avg Markup",
                        value = String.format(Locale.US, "%.1f%%", stats.avgMarginPercent)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(title: String, value: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LiveStockAlertBanner(
    stats: InventoryStats,
    onFilterLowStock: () -> Unit,
    onFilterOutOfStock: () -> Unit
) {
    val hasAlerts = stats.lowStockCount > 0 || stats.outOfStockCount > 0

    AnimatedVisibility(
        visible = hasAlerts,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(250)) + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .animateContentSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (stats.outOfStockCount > 0) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (stats.outOfStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Stock Alert",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (stats.outOfStockCount > 0) "Immediate Reorder Required" else "Low Stock Warning",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (stats.outOfStockCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = buildString {
                                if (stats.outOfStockCount > 0) append("${stats.outOfStockCount} out of stock")
                                if (stats.outOfStockCount > 0 && stats.lowStockCount > 0) append(" • ")
                                if (stats.lowStockCount > 0) append("${stats.lowStockCount} running low")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (stats.outOfStockCount > 0) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (stats.outOfStockCount > 0) {
                        Surface(
                            onClick = onFilterOutOfStock,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "Out (${stats.outOfStockCount})",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                    if (stats.lowStockCount > 0) {
                        Surface(
                            onClick = onFilterLowStock,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = "Low (${stats.lowStockCount})",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBarSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    showSortMenu: Boolean,
    onToggleSortMenu: () -> Unit,
    allItems: List<ElectronicsItem> = emptyList(),
    onQuickRegisterModel: ((String) -> Unit)? = null,
    onOpenHierarchyFilter: (category: String, size: String?) -> Unit
) {
    // Model Auto-Fill suggestions computed as the user types
    val modelSuggestions = remember(allItems, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            emptyList()
        } else {
            allItems
                .filter { item ->
                    item.model.contains(query, ignoreCase = true) ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.brand.contains(query, ignoreCase = true) ||
                    item.sku.contains(query, ignoreCase = true)
                }
                .distinctBy { it.model.ifBlank { it.name } }
                .sortedWith(
                    compareByDescending<ElectronicsItem> {
                        it.model.startsWith(query, ignoreCase = true)
                    }.thenByDescending {
                        it.brand.startsWith(query, ignoreCase = true)
                    }.thenByDescending {
                        it.quantity > 0
                    }
                )
                .take(5)
        }
    }

    // Popular models for quick fill when search is empty or focused
    val quickPopularModels = remember(allItems) {
        val distinctModels = allItems
            .map { it.model.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(6)
        if (distinctModels.isNotEmpty()) distinctModels else listOf("55U6G", "43A6H", "OLED55C3", "BRAVIA-55X90L", "55LE5000", "H55P750UX")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize()
    ) {
        // Main Search Bar with direct Model Number search
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { input ->
                    onSearchQueryChanged(input)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("inventory_search_field"),
                placeholder = { Text("Search model # (55U6G, OLED55C3) or 'LED'...") },
                leadingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.contains("led", ignoreCase = true) || searchQuery.contains("tv", ignoreCase = true)) {
                                onOpenHierarchyFilter("LED TV", null)
                            } else {
                                onOpenHierarchyFilter("LED TV", "55\"")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search / Open Guided Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.contains("led", ignoreCase = true) || searchQuery.contains("tv", ignoreCase = true)) {
                            onOpenHierarchyFilter("LED TV", null)
                        }
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(
                    onClick = onToggleSortMenu,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .testTag("sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort Options",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = onToggleSortMenu
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortOptionSelected(option)
                                onToggleSortMenu()
                            }
                        )
                    }
                }
            }
        }

        // Smooth Auto-Fill Model Suggestions Dropdown Panel
        AnimatedVisibility(
            visible = searchQuery.isNotBlank() && modelSuggestions.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(200)) + expandVertically(),
            exit = fadeOut(animationSpec = tween(150)) + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SubdirectoryArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Suggested Models (Tap to Auto-fill)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${modelSuggestions.size} match${if (modelSuggestions.size > 1) "es" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    modelSuggestions.forEach { suggestion ->
                        val isExactMatch = suggestion.model.equals(searchQuery.trim(), ignoreCase = true)
                        Surface(
                            onClick = {
                                onSearchQueryChanged(suggestion.model.ifBlank { suggestion.name })
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isExactMatch) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .testTag("model_suggestion_${suggestion.model}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = suggestion.model.ifBlank { "N/A" },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = "${suggestion.brand} • ${suggestion.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${suggestion.category}${if (suggestion.size.isNotBlank()) " (${suggestion.size})" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Stock availability badge (greyed out if out of stock!)
                                    val (badgeBg, badgeText, badgeColor) = if (suggestion.isOutOfStock) {
                                        Triple(Color.LightGray.copy(alpha = 0.4f), "Out of Stock", Color.DarkGray)
                                    } else {
                                        Triple(StockInStock.copy(alpha = 0.15f), "${suggestion.quantity} in stock", StockInStock)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = badgeColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Icon(
                                        imageVector = Icons.Default.NorthWest,
                                        contentDescription = "Fill ${suggestion.model}",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Model Not Found -> Register Shortcut
        AnimatedVisibility(
            visible = searchQuery.isNotBlank() && modelSuggestions.isEmpty() && !searchQuery.contains("led", ignoreCase = true) && !searchQuery.contains("tv", ignoreCase = true),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Model \"${searchQuery.trim()}\" not yet registered",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap to quick-register this model in inventory",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (onQuickRegisterModel != null) {
                        Button(
                            onClick = { onQuickRegisterModel(searchQuery.trim()) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Register", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Popular Model Chips (When search query is blank)
        AnimatedVisibility(
            visible = searchQuery.isBlank() && quickPopularModels.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Quick Models:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    quickPopularModels.forEach { modelNum ->
                        Surface(
                            onClick = { onSearchQueryChanged(modelNum) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = modelNum,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.NorthWest,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Guided Hierarchy & Filter Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LED TV Brand > Size > Model Filter (Target of user's request!)
            Surface(
                onClick = { onOpenHierarchyFilter("LED TV", "55\"") },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.testTag("guided_led_filter_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📺 Guided LED Filter (Brand > Size > Model)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Quick pill for Smartphones
            Surface(
                onClick = { onOpenHierarchyFilter("Smartphones", null) },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📱 Phones by Brand",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Contextual Banner if user typed "led" or "tv" in search field
        if (searchQuery.contains("led", ignoreCase = true) || searchQuery.contains("tv", ignoreCase = true)) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                onClick = { onOpenHierarchyFilter("LED TV", null) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LED Filter: Select Size → View All Brands",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Shows Haier, Onida, Sony, Samsung, LG (Out-of-stock brands are greyed out)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Button(
                        onClick = { onOpenHierarchyFilter("LED TV", null) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Open Filter", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CATEGORY_FILTER_OPTIONS.forEach { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun StockFilterChipsRow(
    selectedFilter: StockFilter,
    onFilterSelected: (StockFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StockFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun ElectronicsItemCard(
    item: ElectronicsItem,
    canViewCosts: Boolean,
    isDpUnlocked: Boolean = true,
    onUnlockDp: () -> Unit = {},
    canEdit: Boolean,
    canDelete: Boolean,
    onViewDetail: () -> Unit,
    onSell: () -> Unit,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRequestStock: (() -> Unit)? = null
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        onClick = onViewDetail,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize()
            .testTag("item_card_${item.sku}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Stock Status Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (item.size.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Size: ${item.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Stock Badge
                val (stockBg, stockTextColor, stockText) = when {
                    item.isOutOfStock -> Triple(StockOutOfStock.copy(alpha = 0.15f), StockOutOfStock, "Out of Stock (0)")
                    item.isLowStock -> Triple(StockLowStock.copy(alpha = 0.15f), StockLowStock, "Low Stock (${item.quantity})")
                    else -> Triple(StockInStock.copy(alpha = 0.15f), StockInStock, "${item.quantity} In Stock")
                }

                Surface(
                    color = stockBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stockText,
                        color = stockTextColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Title & Brand
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.brand} • SKU: ${item.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.model.isNotBlank()) {
                    Text(
                        text = " • Model: ${item.model}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (item.location.isNotBlank()) {
                Text(
                    text = "📍 Location: ${item.location}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Row (DP PRICE STRICTLY RESTRICTED TO OWNER / CAN_VIEW_COSTS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selling Price (Retail)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(item.sellingPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (canViewCosts) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable { onUnlockDp() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DP Price: ${currencyFormat.format(item.costPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Owner DP Price Protected",
                                modifier = Modifier.size(11.dp),
                                tint = Color(0xFF0F766E)
                            )
                        }
                        Text(
                            text = "Margin: ${String.format(Locale.US, "%.1f%%", item.profitMarginPercent)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.profitMarginPercent > 20.0) StockInStock else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Sell Button
                Button(
                    onClick = onSell,
                    enabled = item.quantity > 0,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sell", fontSize = 12.sp)
                }

                // Adjust Stock Button
                OutlinedButton(
                    onClick = onAdjust,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stock", fontSize = 12.sp)
                }

                // Request Stock Button (for Low Stock or Sales staff)
                if (onRequestStock != null && (item.isLowStock || item.isOutOfStock || !canViewCosts)) {
                    OutlinedButton(
                        onClick = onRequestStock,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Info / Specs Button
                IconButton(onClick = onViewDetail, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View Specifications & Stock Details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (canEdit) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Item", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// Product Detail & Specifications Sheet Dialog
@Composable
fun ProductDetailDialog(
    item: ElectronicsItem,
    canViewCosts: Boolean,
    onDismiss: () -> Unit,
    onSell: () -> Unit,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onRequestStock: (() -> Unit)? = null
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val (stockBg, stockTextColor, stockText) = when {
        item.isOutOfStock -> Triple(StockOutOfStock.copy(alpha = 0.15f), StockOutOfStock, "Out of Stock (0 units)")
        item.isLowStock -> Triple(StockLowStock.copy(alpha = 0.15f), StockLowStock, "Low Stock Alert (${item.quantity} units remaining)")
        else -> Triple(StockInStock.copy(alpha = 0.15f), StockInStock, "In Stock (${item.quantity} units available)")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getCategoryIcon(item.category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.brand,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                // Stock Health Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = stockBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.isOutOfStock || item.isLowStock) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = stockTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stockText,
                                color = stockTextColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Min: ${item.minStockThreshold}",
                            style = MaterialTheme.typography.labelSmall,
                            color = stockTextColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model, SKU & Location specifications
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailSpecRow(label = "Model #", value = item.model.ifBlank { "Standard" }, isHighlight = true)
                        DetailSpecRow(label = "SKU Barcode", value = item.sku)
                        DetailSpecRow(label = "Category", value = item.category)
                        if (item.size.isNotBlank()) {
                            DetailSpecRow(label = "Screen / Size", value = item.size, isHighlight = true)
                        }
                        DetailSpecRow(label = "Stock Location", value = item.location.ifBlank { "Main Floor" })
                        DetailSpecRow(label = "Warranty", value = "${item.warrantyMonths} Months")
                        if (item.condition.isNotBlank()) {
                            DetailSpecRow(label = "Condition", value = item.condition)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pricing Card (DP Price is Owner Only)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Retail Selling Price",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currencyFormat.format(item.sellingPrice),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (canViewCosts) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "DP Price: ${currencyFormat.format(item.costPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Owner DP Price",
                                            modifier = Modifier.size(11.dp),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    val margin = item.profitMarginPercent
                                    Text(
                                        text = "Margin: ${String.format(Locale.US, "%.1f%%", margin)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (margin >= 20.0) StockInStock else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (canViewCosts) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Value in Stock:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currencyFormat.format(item.sellingPrice * item.quantity),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onRequestStock != null) {
                    OutlinedButton(
                        onClick = onRequestStock,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request Stock")
                    }
                }
                OutlinedButton(
                    onClick = onAdjust,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stock")
                }
                Button(
                    onClick = onSell,
                    enabled = item.quantity > 0,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sell")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun DetailSpecRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// Quick Sale / POS Dialog
@Composable
fun QuickSaleDialog(
    item: ElectronicsItem,
    onDismiss: () -> Unit,
    onConfirmSale: (quantitySold: Int, paymentMethod: String, customerName: String) -> Unit
) {
    var quantitySold by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var customerName by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    val paymentOptions = listOf("Cash", "Credit / Debit Card", "QR / UPI", "Bank Transfer")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PointOfSale, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Process Customer Sale")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Product Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Brand: ${item.brand} ${if (item.size.isNotBlank()) "• Size: ${item.size}" else ""} • Model: ${item.model}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = "SKU: ${item.sku} • In Stock: ${item.quantity} units", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Unit Price: ${currencyFormat.format(item.sellingPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Quantity selector
                Text(text = "Quantity to Sell", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (quantitySold > 1) quantitySold-- },
                        enabled = quantitySold > 1,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "$quantitySold",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )

                    OutlinedButton(
                        onClick = { if (quantitySold < item.quantity) quantitySold++ },
                        enabled = quantitySold < item.quantity,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Total: ${currencyFormat.format(item.sellingPrice * quantitySold)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2E7D32)
                    )
                }

                // Payment Method
                Text(text = "Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentOptions.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                // Customer Name / Phone
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name / Phone (Optional)") },
                    placeholder = { Text("For invoice & warranty record") },
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
                    if (quantitySold > item.quantity) {
                        errorText = "Cannot sell more than available stock (${item.quantity})"
                        return@Button
                    }
                    onConfirmSale(quantitySold, paymentMethod, customerName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.testTag("confirm_sale_button")
            ) {
                Text("Complete Sale (${currencyFormat.format(item.sellingPrice * quantitySold)})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class SaleReceipt(
    val receiptNumber: String,
    val itemName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val customerName: String,
    val salesperson: String,
    val date: String
)

@Composable
fun ReceiptDialog(
    receipt: SaleReceipt,
    onDismiss: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sale Complete • Receipt")
            }
        },
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "MSstock Retail Store", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text(text = "Receipt #${receipt.receiptNumber}", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = receipt.date, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item:", fontWeight = FontWeight.Bold)
                        Text(receipt.itemName, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SKU:")
                        Text(receipt.sku)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Quantity:")
                        Text("${receipt.quantity} units")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Unit Price:")
                        Text(currencyFormat.format(receipt.unitPrice))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Method:")
                        Text(receipt.paymentMethod)
                    }
                    if (receipt.customerName.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer:")
                            Text(receipt.customerName)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sales Rep:")
                        Text(receipt.salesperson)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL PAID:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(currencyFormat.format(receipt.totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun EmptyInventoryPlaceholder(
    isCatalogEmpty: Boolean,
    onAddProduct: () -> Unit,
    onAddBatch: () -> Unit,
    onSeedSample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isCatalogEmpty) "No Electronics in Stock" else "No Matching Products",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isCatalogEmpty) {
                "Your store catalog is ready for your own inventory! Add individual items with model numbers, or use the quick batch wizard to register models."
            } else {
                "Try searching by model number (e.g. 55U6G, 43UIF) or tap 'Guided LED Filter' above."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Button 1: Add New Product & Model
            Button(
                onClick = onAddProduct,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_add_product_button")
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("➕ Add New Product / Model")
            }

            // Button 2: Quick Add Multiple Models (Batch)
            Button(
                onClick = onAddBatch,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_add_batch_button")
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚡ Quick Add Multiple Models (Batch)")
            }

            // Button 3: Optional Sample Catalog
            if (isCatalogEmpty) {
                OutlinedButton(
                    onClick = onSeedSample,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seed_sample_button")
                ) {
                    Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preload Sample Catalog (Optional)")
                }
            }
        }
    }
}
