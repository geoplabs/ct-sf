package com.sustainability.accessmanagement.model

import java.time.Instant

/**
 * Represents the possible roles a user can have in a group
 */
enum class UserGroupRole {
    ADMIN,
    CONTRIBUTOR,
    READER,
}

/**
 * Represents the possible states of a user
 */
enum class UserState {
    INVITED,
    ACTIVE,
    DISABLED,
}

/**
 * Type alias for group-to-role mapping
 */
typealias Groups = Map<String, UserGroupRole>

/**
 * Type alias for tags (key-value pairs)
 * Changed from Map<String, String> to Map<String, Any> to support complex JSON structures
 */
typealias Tags = Map<String, Any>

/**
 * Represents a user in the system with all its attributes
 */
data class User(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val state: UserState? = null,
    val groups: Groups? = mapOf(),
    val tags: Tags? = mapOf(),
    val defaultGroup: String? = null,
    val createdAt: Instant? = null,
    val createdBy: String? = null,
    val updatedAt: Instant? = null,
    val updatedBy: String? = null,
)

/**
 * Request object for creating a new user
 */
data class NewUserRequest(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: UserGroupRole,
    val password: String? = null,
    val tags: Tags? = null,
    val defaultGroup: String? = null,
)

/**
 * Request object for editing an existing user
 */
data class EditUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val password: String? = null,
    val state: UserState? = null,
    val tags: Tags? = null,
    val defaultGroup: String? = null,
)

/**
 * Response object for a list of users with pagination
 */
data class UserListResponse(
    val users: List<User>,
    val pagination: PaginationResponse? = null,
)

/**
 * Pagination information
 */
data class PaginationResponse(
    val lastEvaluatedToken: String? = null,
)

/**
 * Options for listing users
 */
data class UserListOptions(
    val count: Int? = null,
    val exclusiveStart: UserListPaginationKey? = null,
    val tags: Tags? = null,
    val includeChildGroups: Boolean? = null,
    val includeParentGroups: Boolean? = null,
)

/**
 * Pagination key for user listing
 */
data class UserListPaginationKey(
    val paginationToken: String? = null,
) 
