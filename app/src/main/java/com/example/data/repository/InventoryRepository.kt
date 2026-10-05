package com.example.data.repository

import android.content.Context
import com.example.data.local.ElectronicsItemEntity
import com.example.data.local.MSStockDatabase
import com.example.data.local.StaffEntity
import com.example.data.local.StockLogEntity
import com.example.data.local.StockRequestEntity
import com.example.data.model.ElectronicsItem
import com.example.data.model.StaffMember
import com.example.data.model.StaffRole
import com.example.data.model.StockLog
import com.example.data.model.StockRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class InventoryRepository(
    private val database: MSStockDatabase
) {
    constructor(context: Context) : this(MSStockDatabase.getInstance(context))

    private val itemDao = database.electronicsItemDao()
    private val staffDao = database.staffDao()
    private val stockLogDao = database.stockLogDao()
    private val stockRequestDao = database.stockRequestDao()

    fun observeItems(userId: String = "store_main"): Flow<List<ElectronicsItem>> {
        return itemDao.getAllItems().map { list ->
            list.map { it.toModel() }
        }
    }

    fun observeStockLogs(userId: String = "store_main"): Flow<List<StockLog>> {
        return stockLogDao.getAllLogs().map { list ->
            list.map { it.toModel() }
        }
    }

    fun observeStaffMembers(userId: String = "store_main"): Flow<List<StaffMember>> {
        return staffDao.getAllStaff().map { list ->
            list.map { it.toModel() }
        }
    }

    fun observeStockRequests(): Flow<List<StockRequest>> {
        return stockRequestDao.getAllRequests().map { list ->
            list.map { it.toModel() }
        }
    }

    fun observePendingStockRequests(): Flow<List<StockRequest>> {
        return stockRequestDao.getPendingRequests().map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun getItem(userId: String = "store_main", itemId: String): Result<ElectronicsItem?> =
        withContext(Dispatchers.IO) {
            try {
                val entity = itemDao.getItemById(itemId)
                Result.success(entity?.toModel())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createItem(
        item: ElectronicsItem,
        staffName: String = "Store Staff",
        staffRole: String = "OWNER"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val entity = ElectronicsItemEntity.fromModel(item)
            itemDao.insertItem(entity)

            // Record initial stock entry in audit trail if starting quantity > 0
            if (item.quantity > 0) {
                val log = StockLogEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = item.id,
                    itemName = item.name,
                    changeAmount = item.quantity,
                    previousQuantity = 0,
                    newQuantity = item.quantity,
                    reason = "Initial Inventory Entry",
                    staffName = staffName,
                    staffRole = staffRole,
                    createdAt = System.currentTimeMillis()
                )
                stockLogDao.insertLog(log)
            }

            Result.success(item.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createItemsBatch(
        items: List<ElectronicsItem>,
        staffName: String,
        staffRole: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val entities = items.map { ElectronicsItemEntity.fromModel(it) }
            itemDao.insertAll(entities)

            val logs = items.filter { it.quantity > 0 }.map { item ->
                StockLogEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = item.id,
                    itemName = item.name,
                    changeAmount = item.quantity,
                    previousQuantity = 0,
                    newQuantity = item.quantity,
                    reason = "Batch Model Entry",
                    staffName = staffName,
                    staffRole = staffRole,
                    createdAt = System.currentTimeMillis()
                )
            }
            logs.forEach { stockLogDao.insertLog(it) }

            Result.success(items.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItem(item: ElectronicsItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = ElectronicsItemEntity.fromModel(item.copy(updatedAt = System.currentTimeMillis()))
            itemDao.updateItem(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adjustStock(
        userId: String = "store_main",
        itemId: String,
        changeAmount: Int,
        reason: String,
        staffName: String = "Staff",
        staffRole: String = "SALES"
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val existing = itemDao.getItemById(itemId)
                ?: return@withContext Result.failure(IllegalStateException("Item not found in store catalog"))

            val currentQty = existing.quantity
            val newQty = (currentQty + changeAmount).coerceAtLeast(0)
            itemDao.updateQuantity(itemId, newQty, System.currentTimeMillis())

            val log = StockLogEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                itemName = existing.name,
                changeAmount = changeAmount,
                previousQuantity = currentQty,
                newQuantity = newQty,
                reason = reason,
                staffName = staffName,
                staffRole = staffRole,
                createdAt = System.currentTimeMillis()
            )
            stockLogDao.insertLog(log)

            Result.success(newQty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processSale(
        itemId: String,
        quantitySold: Int,
        paymentMethod: String,
        customerName: String,
        staffName: String,
        staffRole: String
    ): Result<Pair<Int, Double>> = withContext(Dispatchers.IO) {
        try {
            val existing = itemDao.getItemById(itemId)
                ?: return@withContext Result.failure(IllegalStateException("Item not found"))

            if (existing.quantity < quantitySold) {
                return@withContext Result.failure(IllegalStateException("Insufficient stock! Available: ${existing.quantity}, Requested: $quantitySold"))
            }

            val currentQty = existing.quantity
            val newQty = currentQty - quantitySold
            val totalSaleAmount = existing.sellingPrice * quantitySold

            itemDao.updateQuantity(itemId, newQty, System.currentTimeMillis())

            val customerNote = if (customerName.isNotBlank()) " | Customer: $customerName" else ""
            val reason = "Sale ($quantitySold units via $paymentMethod$customerNote)"

            val log = StockLogEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                itemName = existing.name,
                changeAmount = -quantitySold,
                previousQuantity = currentQty,
                newQuantity = newQty,
                reason = reason,
                staffName = staffName,
                staffRole = staffRole,
                createdAt = System.currentTimeMillis()
            )
            stockLogDao.insertLog(log)

            Result.success(Pair(newQty, totalSaleAmount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(userId: String = "store_main", itemId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                itemDao.deleteItem(itemId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun saveStaffMember(staff: StaffMember, isUpdate: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val entity = StaffEntity.fromModel(staff.copy(updatedAt = System.currentTimeMillis()))
                if (isUpdate) {
                    staffDao.updateStaff(entity)
                } else {
                    staffDao.insertStaff(entity)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteStaffMember(staffId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            staffDao.deleteStaff(staffId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initializeMasterOwnerIfEmpty(initialPin: String = "apple8901"): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // Ensure PARTH MEHTA is ALWAYS provisioned as the Universal Owner & Admin
                val parthMehta = StaffEntity(
                    id = "owner_parth_mehta",
                    username = "parth",
                    displayName = "PARTH MEHTA",
                    role = StaffRole.OWNER.name,
                    pin = initialPin,
                    phoneOrEmail = "parth.mehta@msstock.store",
                    department = "Universal Owner & Admin",
                    isActive = true,
                    hasHierarchyPermission = true,
                    createdAt = 1000L, // Fixed timestamp so it's always at the top of the staff roster
                    updatedAt = System.currentTimeMillis()
                )
                staffDao.insertStaff(parthMehta)

                // Clean up any old generic "Store Owner" placeholder if present
                val oldOwner = staffDao.getStaffById("owner_default")
                if (oldOwner != null && (oldOwner.displayName == "Store Owner" || oldOwner.username == "owner")) {
                    staffDao.deleteStaff("owner_default")
                }

                // If only PARTH MEHTA exists, also seed a sample sales profile
                val count = staffDao.getStaffCount()
                if (count <= 1) {
                    val sampleSales = StaffEntity(
                        id = UUID.randomUUID().toString(),
                        username = "alex.sales",
                        displayName = "Alex Taylor",
                        role = StaffRole.SALES.name,
                        pin = "1111",
                        phoneOrEmail = "+1 555-0144",
                        department = "Mobile & Audio",
                        isActive = true,
                        hasHierarchyPermission = false, // Owner must grant permission!
                        createdAt = System.currentTimeMillis() + 100,
                        updatedAt = System.currentTimeMillis() + 100
                    )
                    staffDao.insertStaff(sampleSales)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun seedSampleCatalog(userId: String = "store_main"): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val sampleItems = listOf(
                    // LED TVs - Haier
                    ElectronicsItem(
                        id = "item_led_haier_55",
                        name = "Haier 55\" 4K Bezel-Less Google LED TV",
                        sku = "ELEC-TV-H55U6G",
                        category = "LED TV",
                        brand = "Haier",
                        size = "55\"",
                        model = "55U6G",
                        quantity = 5,
                        minStockThreshold = 2,
                        costPrice = 420.00,
                        sellingPrice = 549.99,
                        condition = "NEW",
                        location = "Warehouse Bay TV-02",
                        warrantyMonths = 24,
                        notes = "4K HDR, Dolby Audio, Google TV, 3x HDMI"
                    ),
                    ElectronicsItem(
                        id = "item_led_haier_43",
                        name = "Haier 43\" Full HD Smart LED TV",
                        sku = "ELEC-TV-H43K66",
                        category = "LED TV",
                        brand = "Haier",
                        size = "43\"",
                        model = "43K6600",
                        quantity = 3,
                        minStockThreshold = 2,
                        costPrice = 280.00,
                        sellingPrice = 369.99,
                        condition = "NEW",
                        location = "Warehouse Bay TV-01",
                        warrantyMonths = 24,
                        notes = "Bezel-less design, Android TV, Chromecast built-in"
                    ),

                    // LED TVs - Onida (Out of Stock in 55" to demonstrate greyed out brand!)
                    ElectronicsItem(
                        id = "item_led_onida_55",
                        name = "Onida 55\" 4K UHD Fire TV Edition LED",
                        sku = "ELEC-TV-ON55UIF",
                        category = "LED TV",
                        brand = "Onida",
                        size = "55\"",
                        model = "55UIF",
                        quantity = 0, // Out of stock -> MUST TURN GREY!
                        minStockThreshold = 2,
                        costPrice = 380.00,
                        sellingPrice = 479.99,
                        condition = "NEW",
                        location = "Aisle Display 4",
                        warrantyMonths = 12,
                        notes = "Fire TV OS, Alexa voice remote, Dolby Vision"
                    ),
                    ElectronicsItem(
                        id = "item_led_onida_32",
                        name = "Onida 32\" HD Ready Smart LED TV",
                        sku = "ELEC-TV-ON32HIF",
                        category = "LED TV",
                        brand = "Onida",
                        size = "32\"",
                        model = "32HIF",
                        quantity = 7,
                        minStockThreshold = 3,
                        costPrice = 135.00,
                        sellingPrice = 189.99,
                        condition = "NEW",
                        location = "Shelf TV-Small-1",
                        warrantyMonths = 12,
                        notes = "20W speakers, Dual Band Wi-Fi, Fire TV"
                    ),

                    // LED TVs - Sony
                    ElectronicsItem(
                        id = "item_led_sony_55",
                        name = "Sony BRAVIA 55\" 4K Ultra HD Smart LED TV",
                        sku = "ELEC-TV-SNY55X74",
                        category = "LED TV",
                        brand = "Sony",
                        size = "55\"",
                        model = "KD-55X74L",
                        quantity = 4,
                        minStockThreshold = 2,
                        costPrice = 580.00,
                        sellingPrice = 749.00,
                        condition = "NEW",
                        location = "Front Showcase 1",
                        warrantyMonths = 24,
                        notes = "X1 4K Processor, Live Color, Google TV, Motionflow XR"
                    ),
                    ElectronicsItem(
                        id = "item_led_sony_65",
                        name = "Sony BRAVIA XR 65\" Full Array 4K LED",
                        sku = "ELEC-TV-SNY65X90",
                        category = "LED TV",
                        brand = "Sony",
                        size = "65\"",
                        model = "XR-65X90L",
                        quantity = 2,
                        minStockThreshold = 1,
                        costPrice = 1100.00,
                        sellingPrice = 1399.00,
                        condition = "NEW",
                        location = "Main Center Island",
                        warrantyMonths = 24,
                        notes = "Cognitive Processor XR, Perfect for PS5, 120Hz"
                    ),

                    // LED TVs - Samsung
                    ElectronicsItem(
                        id = "item_led_samsung_55",
                        name = "Samsung 55\" Crystal 4K Vivid Pro LED TV",
                        sku = "ELEC-TV-SAM55CU",
                        category = "LED TV",
                        brand = "Samsung",
                        size = "55\"",
                        model = "55CU7700",
                        quantity = 6,
                        minStockThreshold = 3,
                        costPrice = 460.00,
                        sellingPrice = 599.00,
                        condition = "NEW",
                        location = "Warehouse Bay TV-04",
                        warrantyMonths = 24,
                        notes = "PurColor, Crystal Processor 4K, Smart Hub, Q-Symphony"
                    ),
                    ElectronicsItem(
                        id = "item_led_samsung_43",
                        name = "Samsung 43\" Crystal 4K Neo LED TV",
                        sku = "ELEC-TV-SAM43CU",
                        category = "LED TV",
                        brand = "Samsung",
                        size = "43\"",
                        model = "43CU7700",
                        quantity = 0, // Out of stock in 43" -> turns grey!
                        minStockThreshold = 2,
                        costPrice = 330.00,
                        sellingPrice = 419.00,
                        condition = "NEW",
                        location = "Warehouse Bay TV-04",
                        warrantyMonths = 24,
                        notes = "HDR10+, SolarCell Remote, Motion Xcelerator"
                    ),

                    // LED TVs - LG
                    ElectronicsItem(
                        id = "item_led_lg_55",
                        name = "LG 55\" 4K UHD Smart WebOS LED TV",
                        sku = "ELEC-TV-LG55UR",
                        category = "LED TV",
                        brand = "LG",
                        size = "55\"",
                        model = "55UR7500",
                        quantity = 4,
                        minStockThreshold = 2,
                        costPrice = 440.00,
                        sellingPrice = 579.00,
                        condition = "NEW",
                        location = "Aisle Display 2",
                        warrantyMonths = 24,
                        notes = "α5 AI Gen6, Magic Remote, Filmmaker Mode, Game Dashboard"
                    ),
                    ElectronicsItem(
                        id = "item_led_lg_65",
                        name = "LG C3 65\" 4K OLED evo Smart TV",
                        sku = "ELEC-TV-LGC365",
                        category = "LED TV",
                        brand = "LG",
                        size = "65\"",
                        model = "OLED65C3",
                        quantity = 2,
                        minStockThreshold = 1,
                        costPrice = 1399.00,
                        sellingPrice = 1799.00,
                        condition = "NEW",
                        location = "Backroom Bay TV-03",
                        warrantyMonths = 24,
                        notes = "α9 AI Processor Gen6, 4x HDMI 2.1, NVIDIA G-Sync & FreeSync"
                    ),

                    // LED TVs - TCL (Out of stock in 55"!)
                    ElectronicsItem(
                        id = "item_led_tcl_55",
                        name = "TCL 55\" 4K QLED Dolby Vision Smart TV",
                        sku = "ELEC-TV-TCL55C6",
                        category = "LED TV",
                        brand = "TCL",
                        size = "55\"",
                        model = "55C645",
                        quantity = 0, // Out of stock -> turns grey!
                        minStockThreshold = 2,
                        costPrice = 390.00,
                        sellingPrice = 499.00,
                        condition = "NEW",
                        location = "Shelf TV-05",
                        warrantyMonths = 12,
                        notes = "Quantum Dot, 120Hz DLG, AiPQ Engine 3.0"
                    ),
                    ElectronicsItem(
                        id = "item_led_tcl_43",
                        name = "TCL 43\" 4K UHD Bezel-Less Google TV",
                        sku = "ELEC-TV-TCL43P6",
                        category = "LED TV",
                        brand = "TCL",
                        size = "43\"",
                        model = "43P635",
                        quantity = 4,
                        minStockThreshold = 2,
                        costPrice = 250.00,
                        sellingPrice = 319.99,
                        condition = "NEW",
                        location = "Shelf TV-05",
                        warrantyMonths = 12,
                        notes = "Dynamic Color Enhancement, HDR 10, Dolby Audio"
                    ),

                    // Smartphones
                    ElectronicsItem(
                        id = "item_ip16pm",
                        name = "Apple iPhone 16 Pro Max 256GB",
                        sku = "ELEC-IP16PM-256",
                        category = "Smartphones",
                        brand = "Apple",
                        size = "6.9\"",
                        model = "A3296",
                        quantity = 12,
                        minStockThreshold = 4,
                        costPrice = 989.00,
                        sellingPrice = 1199.00,
                        condition = "NEW",
                        location = "Front Display Case 2",
                        warrantyMonths = 12,
                        notes = "Natural Titanium, Camera Control, 48MP Fusion Camera"
                    ),
                    ElectronicsItem(
                        id = "item_s24u",
                        name = "Samsung Galaxy S24 Ultra 512GB",
                        sku = "ELEC-S24U-512",
                        category = "Smartphones",
                        brand = "Samsung",
                        size = "6.8\"",
                        model = "SM-S928B",
                        quantity = 8,
                        minStockThreshold = 3,
                        costPrice = 1049.00,
                        sellingPrice = 1299.00,
                        condition = "NEW",
                        location = "Front Display Case 3",
                        warrantyMonths = 24,
                        notes = "Galaxy AI, 200MP Quad Tele, S-Pen included"
                    ),

                    // Audio
                    ElectronicsItem(
                        id = "item_sony_xm5",
                        name = "Sony WH-1000XM5 Wireless Headphones",
                        sku = "ELEC-SNY-XM5",
                        category = "Audio & Headphones",
                        brand = "Sony",
                        size = "Over-Ear",
                        model = "WH1000XM5",
                        quantity = 15,
                        minStockThreshold = 5,
                        costPrice = 269.00,
                        sellingPrice = 399.00,
                        condition = "NEW",
                        location = "Audio Aisle Rack 1",
                        warrantyMonths = 12,
                        notes = "Industry leading Noise Cancellation, 30hr battery"
                    )
                )

                itemDao.insertAll(sampleItems.map { ElectronicsItemEntity.fromModel(it) })
                Result.success(sampleItems.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun clearAllInventory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            itemDao.deleteAllItems()
            stockLogDao.deleteAllLogs()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createStockRequest(request: StockRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val entity = StockRequestEntity.fromModel(request)
            stockRequestDao.insertRequest(entity)
            Result.success(request.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveStockRequest(
        requestId: String,
        addQuantityToStock: Boolean = true,
        reviewerName: String = "PARTH MEHTA",
        reviewerRole: String = "OWNER",
        reviewNote: String = "Approved by Owner"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = stockRequestDao.getRequestById(requestId)
                ?: return@withContext Result.failure(IllegalStateException("Request not found"))

            if (addQuantityToStock && request.itemId.isNotBlank()) {
                val item = itemDao.getItemById(request.itemId)
                if (item != null) {
                    val currentQty = item.quantity
                    val newQty = currentQty + request.requestedQuantity
                    itemDao.updateQuantity(item.id, newQty, System.currentTimeMillis())

                    // Log audit trail for restock
                    val log = StockLogEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = item.id,
                        itemName = item.name,
                        changeAmount = request.requestedQuantity,
                        previousQuantity = currentQty,
                        newQuantity = newQty,
                        reason = "Stock Request #${request.id.take(6).uppercase()} Fulfilled (+${request.requestedQuantity}) - Req by ${request.requestedByStaffName}",
                        staffName = reviewerName,
                        staffRole = reviewerRole,
                        createdAt = System.currentTimeMillis()
                    )
                    stockLogDao.insertLog(log)
                }
            }

            stockRequestDao.updateStatus(
                id = requestId,
                status = "APPROVED",
                reviewNote = reviewNote,
                reviewedBy = reviewerName,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectStockRequest(
        requestId: String,
        reviewerName: String = "PARTH MEHTA",
        reviewNote: String = "Request declined"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stockRequestDao.updateStatus(
                id = requestId,
                status = "REJECTED",
                reviewNote = reviewNote,
                reviewedBy = reviewerName,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStockRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stockRequestDao.deleteRequest(requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllStockRequests(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stockRequestDao.deleteAllRequests()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
