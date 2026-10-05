package com.example.ui.inventory

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val stockFilter by viewModel.stockFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val isOperationRunning by viewModel.isOperationRunning.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemToAdjust by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemToSell by remember { mutableStateOf<ElectronicsItem?>(null) }
    var itemToDelete by remember { mutableStateOf<ElectronicsItem?>(null) }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
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

                    // Stock Logs button
                    IconButton(onClick = { showLogsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Audit Activity Log",
                            tint = MaterialTheme.colorScheme.onSurface
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

            // Enhanced Search Bar & Direct Model Search Section
            item {
                SearchBarSection(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    sortOption = sortOption,
                    onSortOptionSelected = viewModel::onSortOptionSelected,
                    showSortMenu = showSortMenu,
                    onToggleSortMenu = { showSortMenu = !showSortMenu },
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
                                onSeedSample = { viewModel.seedSampleCatalog() }
                            )
                        }
                    } else {
                        items(filteredItems, key = { it.id }) { item ->
                            ElectronicsItemCard(
                                item = item,
                                canViewCosts = canViewCosts,
                                canEdit = canEditItemDetails,
                                canDelete = canDeleteItems,
                                onSell = { itemToSell = item },
                                onAdjust = { itemToAdjust = item },
                                onEdit = {
                                    itemToEdit = item
                                    showAddDialog = true
                                },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }
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
private fun SearchBarSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    showSortMenu: Boolean,
    onToggleSortMenu: () -> Unit,
    onOpenHierarchyFilter: (category: String, size: String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
                placeholder = { Text("Search model # (55U6G, 55UIF) or type 'LED'...") },
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
    canEdit: Boolean,
    canDelete: Boolean,
    onSell: () -> Unit,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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

            // Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selling Price",
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
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Cost: ${currencyFormat.format(item.costPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Sell Button (Available to Sales, Managers, Owners)
                Button(
                    onClick = onSell,
                    enabled = item.quantity > 0,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sell", fontSize = 13.sp)
                }

                // Adjust Stock Button
                OutlinedButton(
                    onClick = onAdjust,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stock", fontSize = 13.sp)
                }

                if (canEdit) {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Item", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
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
    onSeedSample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
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
                "Your store inventory is currently empty. Tap below to preload sample electronics (Haier, Onida, Sony, Samsung, LG LED TVs, iPhones, Headphones) or add your own items."
            } else {
                "Try searching by model number (e.g. 55U6G, 43A6H) or tap 'Guided LED Filter' above."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (isCatalogEmpty) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSeedSample,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("seed_sample_button")
            ) {
                Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preload Sample Electronics Catalog")
            }
        }
    }
}
