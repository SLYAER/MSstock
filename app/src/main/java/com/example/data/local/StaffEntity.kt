package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.StaffMember

@Entity(tableName = "staff_members")
data class StaffEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val displayName: String,
    val role: String,
    val pin: String,
    val phoneOrEmail: String = "",
    val department: String = "",
    val isActive: Boolean = true,
    val hasHierarchyPermission: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): StaffMember = StaffMember(
        id = id,
        username = username,
        displayName = displayName,
        role = role,
        pin = pin,
        phoneOrEmail = phoneOrEmail,
        department = department,
        isActive = isActive,
        hasHierarchyPermission = hasHierarchyPermission,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: StaffMember): StaffEntity = StaffEntity(
            id = model.id,
            username = model.username,
            displayName = model.displayName,
            role = model.role,
            pin = model.pin,
            phoneOrEmail = model.phoneOrEmail,
            department = model.department,
            isActive = model.isActive,
            hasHierarchyPermission = model.hasHierarchyPermission,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
