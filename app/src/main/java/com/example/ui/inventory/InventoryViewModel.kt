package com.example.ui.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ElectronicsItem
import com.example.data.model.StaffMember
import com.example.data.model.StaffRole
import com.example.data.model.StockLog
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "InventoryVM"

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class StockFilter(val label: String) {
    ALL("All Stock"),
    LOW_STOCK("Low Stock ⚠️"),
    OUT_OF_STOCK("Out of Stock ❌"),
    IN_STOCK("In Stock ✅")
}

enum class SortOption(val label: String) {
    RECENTLY_UPDATED("Recently Updated"),
    NAME_ASC("Name (A–Z)"),
    QUANTITY_LOW_TO_HIGH("Stock (Lowest First)"),
    QUANTITY_HIGH_TO_LOW("Stock (Highest First)"),
    PRICE_HIGH_TO_LOW("Price (High to Low)"),
    PRICE_LOW_TO_HIGH("Price (Low to High)")
}

data class InventoryStats(
    val totalItems: Int = 0,
    val totalUnits: Int = 0,
    val totalCostValue: Double = 0.0,
    val totalRetailValue: Double = 0.0,
    val potentialProfit: Double = 0.0,
    val avgMarginPercent: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0
)

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    // Currently logged in staff member session
    private val _currentStaff = MutableStateFlow<StaffMember?>(null)
    val currentStaff: StateFlow<StaffMember?> = _currentStaff.asStateFlow()

    val rawItemsState: StateFlow<UiState<List<ElectronicsItem>>> =
        repository.observeItems()
            .map<List<ElectronicsItem>, UiState<List<ElectronicsItem>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing items", error)
                emit(UiState.Error(error.message ?: "Failed to load inventory"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val stockLogsState: StateFlow<UiState<List<StockLog>>> =
        repository.observeStockLogs()
            .map<List<StockLog>, UiState<List<StockLog>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing stock logs", error)
                emit(UiState.Error(error.message ?: "Failed to load stock logs"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val staffListState: StateFlow<UiState<List<StaffMember>>> =
        repository.observeStaffMembers()
            .map<List<StaffMember>, UiState<List<StaffMember>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing staff members", error)
                emit(UiState.Error(error.message ?: "Failed to load staff list"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val stockRequestsState: StateFlow<UiState<List<com.example.data.model.StockRequest>>> =
        repository.observeStockRequests()
            .map<List<com.example.data.model.StockRequest>, UiState<List<com.example.data.model.StockRequest>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing stock requests", error)
                emit(UiState.Error(error.message ?: "Failed to load stock requests"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val pendingRequestsCount: StateFlow<Int> = stockRequestsState.map { state ->
        if (state is UiState.Success) state.data.count { it.isPending } else 0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _stockFilter = MutableStateFlow(StockFilter.ALL)
    val stockFilter = _stockFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RECENTLY_UPDATED)
    val sortOption = _sortOption.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    private val _isOperationRunning = MutableStateFlow(false)
    val isOperationRunning = _isOperationRunning.asStateFlow()

    init {
        // Pre-initialize initial Master Owner and sample data if first run
        initializeMasterOwnerIfEmpty()
    }

    fun initializeMasterOwnerIfEmpty(initialPin: String = "apple8901") {
        viewModelScope.launch {
            repository.initializeMasterOwnerIfEmpty(initialPin)
        }
    }

    // Filtered and sorted items derived flow
    val filteredItems: StateFlow<List<ElectronicsItem>> = combine(
        rawItemsState,
        _searchQuery,
        _selectedCategory,
        _stockFilter,
        _sortOption
    ) { rawState, query, category, filter, sort ->
        if (rawState !is UiState.Success) return@combine emptyList()
        val allItems = rawState.data

        allItems.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.model.contains(query, ignoreCase = true) ||
                item.name.contains(query, ignoreCase = true) ||
                item.sku.contains(query, ignoreCase = true) ||
                item.brand.contains(query, ignoreCase = true) ||
                item.size.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || item.category.equals(category, ignoreCase = true)

            val matchesStock = when (filter) {
                StockFilter.ALL -> true
                StockFilter.LOW_STOCK -> item.isLowStock
                StockFilter.OUT_OF_STOCK -> item.isOutOfStock
                StockFilter.IN_STOCK -> !item.isOutOfStock
            }

            matchesQuery && matchesCategory && matchesStock
        }.sortedWith { a, b ->
            when (sort) {
                SortOption.RECENTLY_UPDATED -> b.updatedAt.compareTo(a.updatedAt)
                SortOption.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                SortOption.QUANTITY_LOW_TO_HIGH -> a.quantity.compareTo(b.quantity)
                SortOption.QUANTITY_HIGH_TO_LOW -> b.quantity.compareTo(a.quantity)
                SortOption.PRICE_HIGH_TO_LOW -> b.sellingPrice.compareTo(a.sellingPrice)
                SortOption.PRICE_LOW_TO_HIGH -> a.sellingPrice.compareTo(b.sellingPrice)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Aggregate statistics
    val stats: StateFlow<InventoryStats> = rawItemsState.map { state ->
        if (state !is UiState.Success) return@map InventoryStats()
        val list = state.data
        var units = 0
        var totalCost = 0.0
        var totalRetail = 0.0
        var lowCount = 0
        var outCount = 0

        for (item in list) {
            units += item.quantity
            totalCost += item.totalCostValue
            totalRetail += item.totalRetailValue
            if (item.isOutOfStock) outCount++
            else if (item.isLowStock) lowCount++
        }

        val potentialProfit = totalRetail - totalCost
        val avgMargin = if (totalRetail > 0.0) (potentialProfit / totalRetail) * 100.0 else 0.0

        InventoryStats(
            totalItems = list.size,
            totalUnits = units,
            totalCostValue = totalCost,
            totalRetailValue = totalRetail,
            potentialProfit = potentialProfit,
            avgMarginPercent = avgMargin,
            lowStockCount = lowCount,
            outOfStockCount = outCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = InventoryStats()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onStockFilterSelected(filter: StockFilter) {
        _stockFilter.value = filter
    }

    fun onSortOptionSelected(sort: SortOption) {
        _sortOption.value = sort
    }

    // Staff session management
    fun loginStaff(staff: StaffMember, enteredPin: String): Boolean {
        val isParthMehta = staff.displayName.equals("PARTH MEHTA", ignoreCase = true) ||
            staff.username.equals("parth", ignoreCase = true) ||
            staff.id == "owner_parth_mehta"

        // Universal Owner & Admin login with password apple8901
        if (isParthMehta && enteredPin == "apple8901") {
            _currentStaff.value = staff.copy(
                role = StaffRole.OWNER.name,
                hasHierarchyPermission = true,
                displayName = "PARTH MEHTA"
            )
            _userMessage.value = "Welcome back, PARTH MEHTA (👑 Universal Owner & Admin)"
            return true
        }

        // Universal master password override for Owner accounts
        if (enteredPin == "apple8901" && staff.staffRole == StaffRole.OWNER) {
            _currentStaff.value = staff
            _userMessage.value = "Welcome back, ${staff.displayName} (👑 Owner)"
            return true
        }

        if (staff.pin.isBlank()) {
            _userMessage.value = "Account pending: Store Owner has not assigned a password yet"
            return false
        }

        if (staff.pin == enteredPin) {
            _currentStaff.value = staff
            _userMessage.value = "Welcome back, ${staff.displayName} (${staff.staffRole.badge})"
            return true
        } else {
            _userMessage.value = "Incorrect password for ${staff.displayName}"
            return false
        }
    }

    fun logoutStaff() {
        _currentStaff.value = null
        _userMessage.value = "Signed out of staff session"
    }

    // New profile registration by staff members
    fun registerStaffProfile(
        displayName: String,
        username: String,
        phoneOrEmail: String,
        department: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val newStaff = StaffMember(
                id = UUID.randomUUID().toString(),
                username = username.trim().lowercase(),
                displayName = displayName.trim(),
                role = StaffRole.SALES.name,
                pin = "", // Blank password indicates pending owner assignment
                phoneOrEmail = phoneOrEmail.trim(),
                department = department.trim(),
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.saveStaffMember(newStaff, isUpdate = false)
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = "Profile registered for $displayName! Ask the Store Owner to assign your password and role."
                onComplete()
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to register profile"
            }
        }
    }

    fun saveStaffMember(staff: StaffMember, isUpdate: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val result = repository.saveStaffMember(staff, isUpdate)
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = if (isUpdate) {
                    "Staff ${staff.displayName} updated (Role: ${staff.staffRole.label})"
                } else {
                    "Staff ${staff.displayName} added with role ${staff.staffRole.label}"
                }
                // If currently logged-in user was updated, refresh session
                if (_currentStaff.value?.id == staff.id) {
                    _currentStaff.value = staff
                }
                onComplete()
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save staff member"
            }
        }
    }

    fun deleteStaffMember(staffId: String) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val result = repository.deleteStaffMember(staffId)
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = "Staff profile removed"
                if (_currentStaff.value?.id == staffId) {
                    _currentStaff.value = null
                }
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to remove staff"
            }
        }
    }

    fun saveItem(item: ElectronicsItem, isUpdate: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val staff = _currentStaff.value
            val staffName = staff?.displayName ?: "Owner"
            val staffRole = staff?.role ?: "OWNER"

            val result = if (isUpdate) {
                repository.updateItem(item)
            } else {
                repository.createItem(item, staffName, staffRole)
            }

            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = if (isUpdate) "Item '${item.name}' updated" else "Item '${item.name}' added"
                onComplete()
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save item"
            }
        }
    }

    fun saveItemsBatch(items: List<ElectronicsItem>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val staff = _currentStaff.value
            val staffName = staff?.displayName ?: "Owner"
            val staffRole = staff?.role ?: "OWNER"

            val result = repository.createItemsBatch(items, staffName, staffRole)
            _isOperationRunning.value = false
            if (result.isSuccess) {
                val count = result.getOrNull() ?: items.size
                _userMessage.value = "Successfully registered $count models in inventory"
                onComplete()
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to add models batch"
            }
        }
    }

    fun quickAdjustStock(item: ElectronicsItem, delta: Int, reason: String) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val staff = _currentStaff.value
            val staffName = staff?.displayName ?: "Store Staff"
            val staffRole = staff?.role ?: "SALES"

            val result = repository.adjustStock(
                itemId = item.id,
                changeAmount = delta,
                reason = reason,
                staffName = staffName,
                staffRole = staffRole
            )
            _isOperationRunning.value = false
            if (result.isSuccess) {
                val newQty = result.getOrNull() ?: 0
                _userMessage.value = "${item.name} stock updated to $newQty ($reason)"
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to adjust stock"
            }
        }
    }

    // Process a customer sale from the Sales / POS counter
    fun processSale(
        item: ElectronicsItem,
        quantitySold: Int,
        paymentMethod: String,
        customerName: String,
        onComplete: (totalSale: Double) -> Unit
    ) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val staff = _currentStaff.value
            val staffName = staff?.displayName ?: "Cashier"
            val staffRole = staff?.role ?: "SALES"

            val result = repository.processSale(
                itemId = item.id,
                quantitySold = quantitySold,
                paymentMethod = paymentMethod,
                customerName = customerName,
                staffName = staffName,
                staffRole = staffRole
            )
            _isOperationRunning.value = false
            if (result.isSuccess) {
                val (_, totalSale) = result.getOrNull() ?: Pair(0, 0.0)
                _userMessage.value = "Sale completed! $quantitySold x ${item.name} sold for $${String.format("%.2f", totalSale)}"
                onComplete(totalSale)
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Sale failed"
            }
        }
    }

    fun deleteItem(item: ElectronicsItem) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val result = repository.deleteItem(itemId = item.id)
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = "Item '${item.name}' deleted"
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete item"
            }
        }
    }

    fun seedSampleCatalog() {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val result = repository.seedSampleCatalog()
            _isOperationRunning.value = false
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _userMessage.value = "Loaded $count popular electronics items into MSstock catalog!"
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to load sample data"
            }
        }
    }

    fun requestStock(
        item: ElectronicsItem,
        requestedQuantity: Int,
        urgency: String,
        note: String
    ) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val staff = _currentStaff.value
            val staffId = staff?.id ?: "sales_staff"
            val staffName = staff?.displayName ?: "Sales Associate"
            val staffRole = staff?.role ?: "SALES"

            val request = com.example.data.model.StockRequest(
                itemId = item.id,
                itemSku = item.sku,
                itemModel = item.model,
                itemName = item.name,
                itemBrand = item.brand,
                itemCategory = item.category,
                currentStock = item.quantity,
                requestedQuantity = requestedQuantity,
                urgency = urgency,
                note = note,
                requestedByStaffId = staffId,
                requestedByStaffName = staffName,
                requestedByStaffRole = staffRole,
                status = "PENDING"
            )

            val result = repository.createStockRequest(request)
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = "Restock request for $requestedQuantity x ${item.name} sent to Owner!"
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to submit restock request"
            }
        }
    }

    fun approveStockRequest(
        requestId: String,
        addStock: Boolean = true,
        reviewNote: String = "Approved by Owner"
    ) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val staff = _currentStaff.value
            val reviewerName = staff?.displayName ?: "PARTH MEHTA"
            val reviewerRole = staff?.role ?: "OWNER"

            val result = repository.approveStockRequest(
                requestId = requestId,
                addQuantityToStock = addStock,
                reviewerName = reviewerName,
                reviewerRole = reviewerRole,
                reviewNote = reviewNote
            )
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = "Stock request approved and units added to live inventory!"
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to approve request"
            }
        }
    }

    fun rejectStockRequest(requestId: String, reason: String) {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val staff = _currentStaff.value
            val reviewerName = staff?.displayName ?: "PARTH MEHTA"

            val result = repository.rejectStockRequest(
                requestId = requestId,
                reviewerName = reviewerName,
                reviewNote = reason
            )
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = "Stock request declined"
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to reject request"
            }
        }
    }

    fun deleteStockRequest(requestId: String) {
        viewModelScope.launch {
            repository.deleteStockRequest(requestId)
        }
    }

    fun clearAllInventory() {
        viewModelScope.launch {
            _isOperationRunning.value = true
            val result = repository.clearAllInventory()
            _isOperationRunning.value = false
            if (result.isSuccess) {
                _userMessage.value = "All inventory items cleared! You can now add your own store catalog."
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to clear inventory"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
