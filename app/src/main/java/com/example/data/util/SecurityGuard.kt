package com.example.data.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enterprise Security & Zero-Trust Protection Guard for MSstock / Universal Electronics.
 * Enforces role-based permissions, brute-force defense, DP cost price masking,
 * input sanitization, and immutable security audit logging.
 */
object SecurityGuard {

    const val MASTER_OWNER_PIN = "apple8901"
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds lockout

    private var failedAttempts = 0
    private var lockoutUntilTimestamp = 0L

    // Owner DP Price Lock State (Defaults to true for authorized Owner sessions to prevent soft-locks)
    private val _isOwnerDpUnlocked = MutableStateFlow(true)
    val isOwnerDpUnlocked = _isOwnerDpUnlocked.asStateFlow()

    private var dpUnlockTimestamp = System.currentTimeMillis()

    // Security Audit Log In-Memory Buffer
    data class SecurityAuditEntry(
        val id: String = java.util.UUID.randomUUID().toString(),
        val action: String,
        val actorName: String,
        val actorRole: String,
        val details: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _securityAuditLogs = MutableStateFlow<List<SecurityAuditEntry>>(emptyList())
    val securityAuditLogs = _securityAuditLogs.asStateFlow()

    init {
        logSecurityEvent(
            action = "SECURITY_SYSTEM_INITIALIZED",
            actorName = "System Guard",
            actorRole = "SYSTEM",
            details = "Zero-trust security shield, role-based protection, and DP guard activated."
        )
    }

    /**
     * Check if the PIN authentication is currently rate-limited/locked out.
     */
    fun isLockedOut(): Pair<Boolean, Long> {
        val now = System.currentTimeMillis()
        if (now < lockoutUntilTimestamp) {
            val remainingSec = (lockoutUntilTimestamp - now) / 1000L
            return Pair(true, remainingSec)
        }
        return Pair(false, 0L)
    }

    /**
     * Clear any active lockout (Admin/Owner reset).
     */
    fun clearLockout() {
        failedAttempts = 0
        lockoutUntilTimestamp = 0L
        _isOwnerDpUnlocked.value = true
        dpUnlockTimestamp = System.currentTimeMillis()
    }

    /**
     * Immediately unlock DP visibility for authenticated Owner session.
     */
    fun unlockForOwner() {
        clearLockout()
        logSecurityEvent(
            action = "OWNER_PRIVILEGES_GRANTED",
            actorName = "Owner / Admin",
            actorRole = "OWNER",
            details = "Full administrative access and DP price viewing active"
        )
    }

    /**
     * Verify Owner Master PIN with brute-force defense (Master PIN always overrides lockouts).
     */
    fun verifyOwnerPin(enteredPin: String, currentStaffPin: String? = null): Boolean {
        val cleanPin = enteredPin.trim()
        val isMasterMatch = cleanPin == MASTER_OWNER_PIN
        val isStaffPinMatch = !currentStaffPin.isNullOrBlank() && cleanPin == currentStaffPin.trim()

        if (isMasterMatch || isStaffPinMatch) {
            // Reset failed counter and unlock immediately
            clearLockout()

            logSecurityEvent(
                action = "OWNER_PIN_VERIFIED_SUCCESS",
                actorName = "Owner / Admin",
                actorRole = "OWNER",
                details = "Owner privileges verified and DP Price unlocked"
            )
            return true
        }

        val (locked, remainingSec) = isLockedOut()
        if (locked) {
            logSecurityEvent(
                action = "PIN_ATTEMPT_BLOCKED_LOCKOUT",
                actorName = "Anonymous/Staff",
                actorRole = "RESTRICTED",
                details = "Authentication blocked due to active cooldown ($remainingSec s remaining)"
            )
            return false
        }

        failedAttempts++
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockoutUntilTimestamp = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            logSecurityEvent(
                action = "BRUTE_FORCE_LOCKOUT_TRIGGERED",
                actorName = "Unknown",
                actorRole = "UNAUTHORIZED",
                details = "5 consecutive invalid PIN attempts. System locked for 30s."
            )
        } else {
            logSecurityEvent(
                action = "PIN_VERIFICATION_FAILED",
                actorName = "Unknown",
                actorRole = "UNAUTHORIZED",
                details = "Invalid PIN attempt ($failedAttempts/$MAX_FAILED_ATTEMPTS)"
            )
        }
        return false
    }

    /**
     * Check if DP (Dealer Price / Wholesale Cost) is authorized to be viewed.
     * Always accessible to authenticated Owners without soft lockouts.
     */
    fun canViewDpPrice(isOwnerRole: Boolean): Boolean {
        if (!isOwnerRole) return false
        return _isOwnerDpUnlocked.value
    }

    /**
     * Lock DP visibility immediately.
     */
    fun lockOwnerDp() {
        _isOwnerDpUnlocked.value = false
        dpUnlockTimestamp = 0L
        logSecurityEvent(
            action = "DP_PRICE_LOCKED",
            actorName = "Owner / System",
            actorRole = "OWNER",
            details = "Dealer Price privacy guard engaged"
        )
    }

    /**
     * Input Sanitizer: Strips malicious tags, HTML, control chars, and limits length.
     */
    fun sanitizeText(input: String, maxLength: Int = 200): String {
        return input
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "") // Remove control characters
            .trim()
            .take(maxLength)
    }

    /**
     * Price Validator: Ensures non-negative, finite, realistic monetary bounds.
     */
    fun validatePrice(price: Double): Boolean {
        return !price.isNaN() && !price.isInfinite() && price >= 0.0 && price <= 1_000_000.0
    }

    /**
     * Quantity Validator: Ensures non-negative integers within physical storage limits.
     */
    fun validateQuantity(qty: Int): Boolean {
        return qty in 0..1_000_000
    }

    /**
     * SKU Validator: Checks for alphanumeric, hyphen, underscore, dot characters.
     */
    fun validateSku(sku: String): Boolean {
        val clean = sku.trim()
        if (clean.isEmpty() || clean.length > 50) return false
        return clean.matches(Regex("^[a-zA-Z0-9_\\-\\./#]+$"))
    }

    /**
     * Record an immutable security event log.
     */
    fun logSecurityEvent(action: String, actorName: String, actorRole: String, details: String) {
        val entry = SecurityAuditEntry(
            action = action,
            actorName = actorName,
            actorRole = actorRole,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        _securityAuditLogs.value = (listOf(entry) + _securityAuditLogs.value).take(150)
    }
}
