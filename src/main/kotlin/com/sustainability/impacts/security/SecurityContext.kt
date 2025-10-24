package com.sustainability.impacts.security

/**
 * Security context for the impacts module.
 * This is used to track user information across API calls.
 */
data class SecurityContext(
    val userId: String,
    val groupId: String,
    val groupRoles: List<String>,
) {
    override fun toString(): String {
        return "ImpactsSecurityContext(userId='$userId', groupId='$groupId', groupRoles=$groupRoles)"
    }
} 
