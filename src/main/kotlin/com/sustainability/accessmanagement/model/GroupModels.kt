package com.sustainability.accessmanagement.model

import java.time.Instant

/**
 * Represents the possible states of a group
 */
enum class GroupState {
    ACTIVE,
    DISABLED,
}

/**
 * Represents a configuration source
 */
typealias ConfigurationSource = Map<String, Any>

/**
 * Represents a configuration
 */
typealias Configuration = Map<String, Any>

/**
 * Represents a group in the system with all its attributes
 */
data class Group(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val state: GroupState? = null,
    val tags: Tags? = null,
    val isParentCompany: Boolean = false,
    val accountId: String? = null,
    val createdBy: String? = null,
    val createdAt: Instant? = null,
    val updatedBy: String? = null,
    val updatedAt: Instant? = null,
    val configuration: Configuration? = null,
    val configurationSource: ConfigurationSource? = null,
)

/**
 * Request object for creating a new group
 */
data class NewGroupRequest(
    val name: String,
    val description: String? = null,
    val tags: Tags? = null,
    val isParentCompany: Boolean = false,
    val accountId: String? = null,
    val configuration: Configuration? = null,
)

/**
 * Request object for editing an existing group
 */
data class EditGroupRequest(
    val description: String? = null,
    val state: GroupState? = null,
    val tags: Tags? = null,
    val isParentCompany: Boolean? = null,
    val accountId: String? = null,
    val configuration: Configuration? = null,
)

/**
 * Response object for a list of groups with pagination
 */
data class GroupListResponse(
    val groups: List<Group>,
    val pagination: PaginationResponse? = null,
)

/**
 * Role assignment for a group
 */
data class GroupRole(
    val role: UserGroupRole,
)

/**
 * Options for listing groups
 */
data class GroupListOptions(
    val count: Int? = null,
    val exclusiveStart: GroupListPaginationKey? = null,
    val tags: Tags? = null,
    val accountId: String? = null,
    val includeChildGroups: Boolean? = null,
    val includeParentGroups: Boolean? = null,
    val showConfigurationSource: Boolean = false,
)

/**
 * Pagination key for group listing
 */
data class GroupListPaginationKey(
    val paginationToken: String? = null,
) 
