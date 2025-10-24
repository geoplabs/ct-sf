package com.sustainability.accessmanagement.model

/**
 * Represents the security context for authorization checks
 */
data class SecurityContext(
    val email: String,
    val groupId: String,
    val groupRoles: Map<String, UserGroupRole>,
    val scope: SecurityScope? = null,
)

/**
 * Represents the possible security scopes
 */
enum class SecurityScope {
    SUPER_ADMIN,
    ADMIN,
    CONTRIBUTOR,
    READER,
}

/**
 * Utility functions for security checks
 */
object SecurityUtils {
    /**
     * Checks if the given roles satisfy the minimum required role for all or any groups
     *
     * @param groupIds The list of group IDs to check
     * @param roles The map of group roles
     * @param minimumRole The minimum required role
     * @param checkType Whether to check all groups or any group
     * @return True if authorized, false otherwise
     */
    fun isAuthorized(
        groupIds: List<String>,
        roles: Map<String, UserGroupRole>,
        minimumRole: UserGroupRole,
        checkType: CheckType,
    ): Boolean {
        if (groupIds.isEmpty()) return false

        val check = { groupId: String ->
            val role = roles[groupId]
            role != null && isRoleAtLeast(role, minimumRole)
        }

        return when (checkType) {
            CheckType.ALL -> groupIds.all(check)
            CheckType.ANY -> groupIds.any(check)
        }
    }

    /**
     * Checks if a role meets or exceeds the minimum required role
     *
     * @param role The role to check
     * @param minimumRole The minimum required role
     * @return True if the role is at least the minimum role, false otherwise
     */
    fun isRoleAtLeast(role: UserGroupRole, minimumRole: UserGroupRole): Boolean {
        return when (minimumRole) {
            UserGroupRole.READER -> true // Any role is at least a reader
            UserGroupRole.CONTRIBUTOR -> role in listOf(UserGroupRole.CONTRIBUTOR, UserGroupRole.ADMIN)
            UserGroupRole.ADMIN -> role == UserGroupRole.ADMIN
        }
    }

    /**
     * Returns true if the role is at least ADMIN
     */
    fun atLeastAdmin(role: UserGroupRole): Boolean = isRoleAtLeast(role, UserGroupRole.ADMIN)

    /**
     * Returns true if the role is at least CONTRIBUTOR
     */
    fun atLeastContributor(role: UserGroupRole): Boolean = isRoleAtLeast(role, UserGroupRole.CONTRIBUTOR)

    /**
     * Returns true if the role is at least READER
     */
    fun atLeastReader(role: UserGroupRole): Boolean = isRoleAtLeast(role, UserGroupRole.READER)
}

/**
 * Check type for authorization
 */
enum class CheckType {
    ALL,
    ANY,
} 
