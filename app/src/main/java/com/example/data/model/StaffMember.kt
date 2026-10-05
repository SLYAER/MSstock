package com.example.data.model

enum class StaffRole(val label: String, val badge: String, val description: String) {
    OWNER("Owner", "👑 Owner", "Full Administrative Access, Financials & Staff Management"),
    MANAGER("Manager", "📦 Manager", "Inventory Management, Stock In/Out & Catalog Audits"),
    SALES("Sales", "💼 Sales", "Product Catalog, Quick POS / Sales & Customer Inquiries"),
    CLERK("Clerk", "🛠️ Clerk", "Restocking, Warehouse & Basic Stock Count")
}

data class StaffMember(
    val id: String = "",
    val userId: String = "store_main",
    val username: String = "",
    val displayName: String = "",
    val role: String = "SALES",
    val pin: String = "", // Assigned password or PIN
    val phoneOrEmail: String = "",
    val department: String = "",
    val isActive: Boolean = true,
    val hasHierarchyPermission: Boolean = false, // Owner-configurable permission for Brand > Size > Model filter
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val staffRole: StaffRole
        get() = try {
            StaffRole.valueOf(role.uppercase())
        } catch (e: Exception) {
            StaffRole.SALES
        }

    val isPendingApproval: Boolean
        get() = pin.isBlank()

    val isOwner: Boolean
        get() = staffRole == StaffRole.OWNER

    val canAccessHierarchy: Boolean
        get() = staffRole == StaffRole.OWNER || hasHierarchyPermission

    val canViewCostsAndMargins: Boolean
        get() = staffRole == StaffRole.OWNER

    val canManageStaff: Boolean
        get() = staffRole == StaffRole.OWNER

    val canDeleteItems: Boolean
        get() = staffRole == StaffRole.OWNER

    val canEditItemDetails: Boolean
        get() = staffRole == StaffRole.OWNER || staffRole == StaffRole.MANAGER

    val canAdjustStock: Boolean
        get() = true

    val canRecordSales: Boolean
        get() = true
}
