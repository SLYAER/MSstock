package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MSStockDatabase
import com.example.data.model.ElectronicsItem
import com.example.data.model.StaffMember
import com.example.data.model.StaffRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class InventoryRepositoryRuleTest {

    private lateinit var database: MSStockDatabase
    private lateinit var repository: InventoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MSStockDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InventoryRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createAndGetItem_succeeds() = runBlocking {
        val itemId = UUID.randomUUID().toString()
        val item = ElectronicsItem(
            id = itemId,
            name = "Apple iPhone 16 Pro 256GB",
            sku = "ELEC-IP16P-256",
            category = "Smartphones",
            brand = "Apple",
            model = "A3293 Natural Titanium",
            quantity = 10,
            minStockThreshold = 2,
            costPrice = 899.0,
            sellingPrice = 1099.0,
            condition = "NEW",
            location = "Display Cabinet A",
            warrantyMonths = 12
        )

        val createResult = repository.createItem(item, "Store Owner", "OWNER")
        assertTrue(createResult.isSuccess)

        val fetchResult = repository.getItem(itemId = itemId)
        assertTrue(fetchResult.isSuccess)
        val fetched = fetchResult.getOrNull()
        assertNotNull(fetched)
        assertEquals("Apple iPhone 16 Pro 256GB", fetched?.name)
        assertEquals(10, fetched?.quantity)
    }

    @Test
    fun processSale_decrementsStock_andLogsSale() = runBlocking {
        val itemId = UUID.randomUUID().toString()
        val item = ElectronicsItem(
            id = itemId,
            name = "Sony WH-1000XM5",
            sku = "ELEC-SNY-XM5",
            category = "Audio & Headphones",
            brand = "Sony",
            quantity = 8,
            sellingPrice = 399.00
        )
        repository.createItem(item)

        // Process a sale of 2 units
        val saleResult = repository.processSale(
            itemId = itemId,
            quantitySold = 2,
            paymentMethod = "Cash",
            customerName = "John Doe",
            staffName = "Alex",
            staffRole = "SALES"
        )
        assertTrue(saleResult.isSuccess)
        val (newQty, totalAmount) = saleResult.getOrNull()!!
        assertEquals(6, newQty)
        assertEquals(798.00, totalAmount, 0.01)

        // Verify updated item in DB
        val updated = repository.getItem(itemId = itemId).getOrNull()
        assertEquals(6, updated?.quantity)

        // Verify stock log was created
        val logs = repository.observeStockLogs().first()
        assertTrue(logs.any { it.itemId == itemId && it.changeAmount == -2 && it.staffRole == "SALES" })
    }

    @Test
    fun staffLifecycle_registerPendingProfile_ownerAssignsPasswordAndRole() = runBlocking {
        // Staff registers profile (password is empty / pending)
        val staffId = UUID.randomUUID().toString()
        val pendingStaff = StaffMember(
            id = staffId,
            username = "sarah.sales",
            displayName = "Sarah Connor",
            role = StaffRole.SALES.name,
            pin = "" // Empty pin = pending approval
        )
        val savePendingResult = repository.saveStaffMember(pendingStaff, isUpdate = false)
        assertTrue(savePendingResult.isSuccess)

        var staffList = repository.observeStaffMembers().first()
        val retrieved = staffList.find { it.id == staffId }
        assertNotNull(retrieved)
        assertTrue(retrieved!!.isPendingApproval)

        // Owner assigns password "4321" and promotes role to MANAGER
        val approvedStaff = retrieved.copy(
            pin = "4321",
            role = StaffRole.MANAGER.name
        )
        val updateResult = repository.saveStaffMember(approvedStaff, isUpdate = true)
        assertTrue(updateResult.isSuccess)

        staffList = repository.observeStaffMembers().first()
        val finalStaff = staffList.find { it.id == staffId }
        assertNotNull(finalStaff)
        assertFalse(finalStaff!!.isPendingApproval)
        assertEquals("4321", finalStaff.pin)
        assertEquals(StaffRole.MANAGER, finalStaff.staffRole)
        assertTrue(finalStaff.canViewCostsAndMargins)
    }
}
