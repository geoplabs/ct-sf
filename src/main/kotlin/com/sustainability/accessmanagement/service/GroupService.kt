package com.sustainability.accessmanagement.service

import com.sustainability.accessmanagement.model.CheckType
import com.sustainability.accessmanagement.model.EditGroupRequest
import com.sustainability.accessmanagement.model.Group
import com.sustainability.accessmanagement.model.GroupListOptions
import com.sustainability.accessmanagement.model.GroupListPaginationKey
import com.sustainability.accessmanagement.model.GroupState
import com.sustainability.accessmanagement.model.InvalidRequestException
import com.sustainability.accessmanagement.model.NewGroupRequest
import com.sustainability.accessmanagement.model.ResourceNotFoundException
import com.sustainability.accessmanagement.model.SecurityContext
import com.sustainability.accessmanagement.model.SecurityUtils
import com.sustainability.accessmanagement.model.UnauthorizedException
import com.sustainability.accessmanagement.model.UserGroupRole
import com.sustainability.accessmanagement.repository.GroupRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant

@Service
class GroupService(
    private val groupRepository: GroupRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Get a group by ID
     */
    fun get(securityContext: SecurityContext, groupId: String, showConfigurationSource: Boolean = false): Group {
        logger.debug("GroupService > get > groupId: {}, showConfigurationSource: {}", groupId, showConfigurationSource)

        // Decode the group ID from URL encoding
        val decodedGroupId = URLDecoder.decode(groupId, StandardCharsets.UTF_8.toString())

        // Get the group
        val group = groupRepository.get(decodedGroupId)
            ?: throw ResourceNotFoundException("Group with ID $decodedGroupId not found")

        // Authorization check: Must have at least reader access to this group or its parent
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.READER,
            CheckType.ALL,
        ) && (
            securityContext.groupId == decodedGroupId ||
                isParentOf(securityContext.groupId, decodedGroupId) ||
                isParentOf(decodedGroupId, securityContext.groupId)
            )

        if (!isAuthorized) {
            throw UnauthorizedException("You don't have permission to access this group")
        }

        return group
    }

    /**
     * Create a new group
     */
    fun create(securityContext: SecurityContext, newGroup: NewGroupRequest): Group {
        logger.debug("GroupService > create > newGroup: {}", newGroup)

        // Authorization check: Must have admin access to create subgroups
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.ADMIN,
            CheckType.ALL,
        )

        if (!isAuthorized) {
            throw UnauthorizedException("You must be an admin to create groups")
        }

        // Clean and format the group name for the ID
        val cleanedName = formatGroupNameForId(newGroup.name)

        if (cleanedName.isEmpty()) {
            throw InvalidRequestException("Group name must contain at least one alphanumeric character")
        }

        // Create group ID (parent group ID + "/" + cleaned group name)
        val groupId = if (securityContext.groupId == "/") {
            "/$cleanedName"
        } else {
            "${securityContext.groupId}/$cleanedName"
        }

        // Check if group already exists
        if (groupRepository.exists(groupId)) {
            throw InvalidRequestException("A group with name $cleanedName already exists in this context")
        }

        // Check if total ID length is valid
        if (groupId.length > 114) {
            throw InvalidRequestException("Group ID would exceed the maximum length of 114 characters")
        }

        // Generate a secure account ID for the company (if it's a parent company)
        val now = Instant.now()
        val accountId = if (newGroup.isParentCompany) {
            // Only generate a new account ID if one wasn't provided
            newGroup.accountId ?: generateSecureAccountId()
        } else {
            newGroup.accountId
        }

        // Create the group
        val group = Group(
            id = groupId,
            name = newGroup.name, // Keep the original name for display purposes
            description = newGroup.description,
            state = GroupState.ACTIVE,
            isParentCompany = newGroup.isParentCompany,
            accountId = accountId,
            tags = newGroup.tags,
            configuration = newGroup.configuration,
            createdAt = now,
            createdBy = securityContext.email,
            updatedAt = now,
            updatedBy = securityContext.email,
        )

        return groupRepository.create(group)
    }

    /**
     * Generate a secure account ID for internal company tracking
     * Following security best practices:
     * - Uses UUID for uniqueness and randomness
     * - Includes timestamp hash for time-based uniqueness
     * - Uses a consistent prefix for identification
     * - Includes checksum for integrity verification
     */
    private fun generateSecureAccountId(): String {
        // Generate a UUID for uniqueness
        val uuid = java.util.UUID.randomUUID().toString()

        // Use current time millis as an additional source of uniqueness
        val timestamp = Instant.now().toEpochMilli()

        // Create a hash of the timestamp (to avoid revealing exact creation time)
        val timestampHash = timestamp.toString().hashCode().toString(16).replace("-", "")

        // Create a combined string
        val combined = "$uuid-$timestampHash"

        // Calculate a simple checksum for integrity verification
        val checksum = combined.sumOf { it.code }.rem(1000).toString().padStart(3, '0')

        // Return with standard prefix
        return "act-$timestampHash-${uuid.substring(0, 8)}-$checksum"
    }

    /**
     * Helper method to format a group name for use in IDs
     * Converts to lowercase, removes special characters, replaces spaces with hyphens
     */
    private fun formatGroupNameForId(name: String): String {
        return name.lowercase()
            .replace("\\s+".toRegex(), "-") // Replace spaces with hyphens
            .replace("[^a-z0-9-]".toRegex(), "") // Remove special characters except hyphens
            .replace("-{2,}".toRegex(), "-") // Replace multiple hyphens with a single one
    }

    /**
     * Update a group
     */
    fun update(securityContext: SecurityContext, groupId: String, updated: EditGroupRequest): Group {
        logger.debug("GroupService > update > groupId: {}, updated: {}", groupId, updated)

        // Decode the group ID from URL encoding
        val decodedGroupId = URLDecoder.decode(groupId, StandardCharsets.UTF_8.toString())

        // Authorization check: Must have admin access to update groups
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.ADMIN,
            CheckType.ALL,
        ) && (
            securityContext.groupId == decodedGroupId ||
                isParentOf(securityContext.groupId, decodedGroupId)
            )

        if (!isAuthorized) {
            throw UnauthorizedException("You must be an admin of this group or its parent to update it")
        }

        // Get the existing group
        val existing = groupRepository.get(decodedGroupId)
            ?: throw ResourceNotFoundException("Group with ID $decodedGroupId not found")

        // Create updated group object
        val now = Instant.now()

        // Handle account ID for parent companies
        val updatedAccountId = if (updated.isParentCompany == true && existing.accountId == null) {
            // If converting to parent company and no account ID exists, generate one
            updated.accountId ?: generateSecureAccountId()
        } else if (updated.accountId != null) {
            // Use provided account ID if available
            updated.accountId
        } else {
            // Keep existing account ID
            existing.accountId
        }

        val updatedGroup = existing.copy(
            description = updated.description ?: existing.description,
            state = updated.state ?: existing.state,
            isParentCompany = updated.isParentCompany ?: existing.isParentCompany,
            accountId = updatedAccountId,
            configuration = mergeMaps(existing.configuration, updated.configuration),
            updatedAt = now,
            updatedBy = securityContext.email,
        )

        // Calculate tag differences
        val existingTags = existing.tags ?: emptyMap()
        val updatedTags = updated.tags ?: emptyMap()

        // Calculate the tags to add/update
        val tagsToAdd = updatedTags.filter { (key, value) ->
            existingTags[key] != value
        }

        // Calculate tags to delete
        val keysToDelete = existingTags.keys.filter { key ->
            !updatedTags.containsKey(key)
        }

        val tagsToDelete = keysToDelete.associateWith { existingTags[it]!! }

        // Update group in database
        return groupRepository.update(updatedGroup, tagsToAdd, tagsToDelete)
    }

    /**
     * Delete a group
     */
    fun delete(securityContext: SecurityContext, groupId: String) {
        logger.debug("GroupService > delete > groupId: {}", groupId)

        // Decode the group ID from URL encoding
        val decodedGroupId = URLDecoder.decode(groupId, StandardCharsets.UTF_8.toString())

        // Cannot delete root group
        if (decodedGroupId == "/") {
            throw InvalidRequestException("Cannot delete the root group")
        }

        // Authorization check: Must have admin access to delete groups
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.ADMIN,
            CheckType.ALL,
        ) && (
            securityContext.groupId == getParentGroupId(decodedGroupId) ||
                securityContext.groupId == decodedGroupId
            )

        if (!isAuthorized) {
            throw UnauthorizedException("You must be an admin of this group or its parent to delete it")
        }

        // Get the group
        val group = groupRepository.get(decodedGroupId)
            ?: throw ResourceNotFoundException("Group with ID $decodedGroupId not found")

        // Check if group has child groups
        val childGroups = groupRepository.list(parentGroupId = decodedGroupId)
        if (childGroups.isNotEmpty()) {
            throw InvalidRequestException("Cannot delete a group that has child groups. Delete child groups first.")
        }

        // Delete the group
        groupRepository.delete(decodedGroupId)
    }

    /**
     * List groups with pagination and filtering
     */
    fun list(
        securityContext: SecurityContext,
        options: GroupListOptions? = null,
    ): Pair<List<Group>, GroupListPaginationKey?> {
        logger.debug("GroupService > list > options: {}", options)

        // Authorization check: Must have at least reader access
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.READER,
            CheckType.ALL,
        )

        if (!isAuthorized) {
            throw UnauthorizedException("You must have at least reader access to list groups")
        }

        // Determine the parent group ID for filtering
        val parentGroupId = if (options?.includeParentGroups == true) {
            // If including parent groups, get up to the root
            null
        } else if (options?.includeChildGroups == true) {
            // If including child groups, use the current group as parent
            securityContext.groupId
        } else {
            // Only get the direct children of the current group
            securityContext.groupId
        }

        // Get groups
        val groups = groupRepository.list(
            limit = options?.count?.plus(1), // Add 1 for pagination detection
            fromGroupId = options?.exclusiveStart?.paginationToken,
            tags = options?.tags,
            accountId = options?.accountId,
            states = null, // Get all states
            parentGroupId = parentGroupId,
        )

        // Filter groups based on security context
        val filteredGroups = groups.filter { group ->
            securityContext.groupId == group.id ||
                isParentOf(securityContext.groupId, group.id) ||
                isParentOf(group.id, securityContext.groupId)
        }

        // Handle pagination
        val paginationKey = if (filteredGroups.size > (options?.count ?: 10)) {
            val lastItem = filteredGroups[options?.count ?: 10 - 1]
            GroupListPaginationKey(paginationToken = lastItem.id)
        } else {
            null
        }

        // Return the correct number of items
        val resultGroups = if (filteredGroups.size > (options?.count ?: 10)) {
            filteredGroups.subList(0, options?.count ?: 10)
        } else {
            filteredGroups
        }

        return Pair(resultGroups, paginationKey)
    }

    /**
     * Helper method to check if a group name is valid
     */
    private fun isValidGroupName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9-]+$"))
    }

    /**
     * Helper method to check if one group ID is a parent of another
     */
    private fun isParentOf(potentialParentId: String, childId: String): Boolean {
        if (potentialParentId == "/") {
            return true // Root is parent of all groups
        }

        return childId.startsWith("$potentialParentId/")
    }

    /**
     * Helper method to get the parent group ID
     */
    private fun getParentGroupId(groupId: String): String {
        if (groupId == "/" || !groupId.contains("/")) {
            return "/" // Root is the parent of top-level groups
        }

        val lastSlashIndex = groupId.lastIndexOf("/")
        if (lastSlashIndex == 0) {
            return "/" // Parent is root
        }

        return groupId.substring(0, lastSlashIndex)
    }

    /**
     * Helper method to merge maps
     */
    private fun <K, V> mergeMaps(map1: Map<K, V>?, map2: Map<K, V>?): Map<K, V> {
        if (map2 == null) {
            return map1 ?: emptyMap()
        }

        val result = map1?.toMutableMap() ?: mutableMapOf()
        map2.forEach { (key, value) -> result[key] = value }
        return result
    }
} 
