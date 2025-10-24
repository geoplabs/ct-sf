package com.sustainability.accessmanagement.service

import com.sustainability.accessmanagement.model.CheckType
import com.sustainability.accessmanagement.model.EditUserRequest
import com.sustainability.accessmanagement.model.Groups
import com.sustainability.accessmanagement.model.InvalidRequestException
import com.sustainability.accessmanagement.model.NewUserRequest
import com.sustainability.accessmanagement.model.ResourceNotFoundException
import com.sustainability.accessmanagement.model.SecurityContext
import com.sustainability.accessmanagement.model.SecurityUtils
import com.sustainability.accessmanagement.model.Tags
import com.sustainability.accessmanagement.model.UnauthorizedException
import com.sustainability.accessmanagement.model.User
import com.sustainability.accessmanagement.model.UserGroupRole
import com.sustainability.accessmanagement.model.UserListOptions
import com.sustainability.accessmanagement.model.UserListPaginationKey
import com.sustainability.accessmanagement.model.UserState
import com.sustainability.accessmanagement.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val groupService: GroupService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Get a user by email
     */
    fun get(securityContext: SecurityContext, email: String, enforceGroupAccessCheck: Boolean = true): User {
        logger.debug("UserService > get > email: {}, enforceGroupAccessCheck: {}", email, enforceGroupAccessCheck)

        // Normalize email to lowercase
        val normalizedEmail = email.lowercase()

        // Get the user
        val user = userRepository.get(normalizedEmail)
            ?: throw ResourceNotFoundException("User with email $normalizedEmail not found")

        // If group access check is required, verify the caller has access to the user's groups
        if (enforceGroupAccessCheck && normalizedEmail != securityContext.email) {
            val isAuthorized = SecurityUtils.isAuthorized(
                listOf(securityContext.groupId),
                securityContext.groupRoles,
                UserGroupRole.READER,
                CheckType.ALL,
            )

            if (!isAuthorized) {
                throw UnauthorizedException("You don't have permission to access this user's details")
            }

            // Extra check: verify the user belongs to the current group context
            if (!user.groups?.containsKey(securityContext.groupId)!!) {
                throw ResourceNotFoundException(
                    "User with email $normalizedEmail not found in group ${securityContext.groupId}",
                )
            }
        }

        return user
    }

    /**
     * Update a user
     */
    fun update(securityContext: SecurityContext, email: String, updated: EditUserRequest): User {
        logger.debug("UserService > update > email: {}, updated: {}", email, updated)

        // Validate request
        if (updated.password == null && updated.defaultGroup == null && updated.state == null && updated.tags == null) {
            throw InvalidRequestException(
                "Request body should contain one or more of these parameters: password, defaultGroup, state, tags",
            )
        }

        // Normalize email to lowercase
        val normalizedEmail = email.lowercase()

        // Authorization check 1: Only allow users to update their own password
        if (updated.password != null) {
            if (normalizedEmail != securityContext.email) {
                throw UnauthorizedException("Users may only update their own password")
            }
        }

        // Authorization check 2: Admin roles may update any user status where they are admin of all the user's groups
        if (updated.state != null) {
            if (normalizedEmail == securityContext.email) {
                throw UnauthorizedException("Users may not update their own state")
            }

            val existingUser = get(securityContext, normalizedEmail)
            val isAuthorized = SecurityUtils.isAuthorized(
                existingUser.groups?.keys?.toList() ?: emptyList(),
                securityContext.groupRoles,
                UserGroupRole.ADMIN,
                CheckType.ALL,
            )

            if (!isAuthorized) {
                throw UnauthorizedException(
                    "The caller is not an admin of all the groups the user belongs to: ${existingUser.groups}",
                )
            }
        }

        // Retrieve existing user for further checks
        val existing = get(securityContext, normalizedEmail)

        // Create updated user object
        val now = Instant.now()
        val updatedUser = existing.copy(
            firstName = updated.firstName ?: existing.firstName,
            lastName = updated.lastName ?: existing.lastName,
            state = updated.state ?: existing.state,
            defaultGroup = if (isChildGroup(existing.groups, updated.defaultGroup)) {
                updated.defaultGroup
            } else {
                existing.defaultGroup
            },
            tags = mergeTags(existing.tags, updated.tags),
            updatedAt = now,
            updatedBy = securityContext.email,
        )

        // If state is changed to disabled, make sure there's at least one admin remaining in the root group
        if (updated.state == UserState.DISABLED && existing.state != UserState.DISABLED) {
            if (existing.groups?.get("/") == UserGroupRole.ADMIN) {
                // Check if this is the last admin in root group
                val rootAdmins = userRepository.list(
                    states = listOf(UserState.ACTIVE, UserState.INVITED),
                ).filter {
                    it.email != normalizedEmail &&
                        it.groups?.get("/") == UserGroupRole.ADMIN
                }

                if (rootAdmins.isEmpty()) {
                    throw InvalidRequestException("User $normalizedEmail is the last remaining admin in the root group")
                }
            }
        }

        // Calculate tag differences
        val existingTags = existing.tags ?: emptyMap()
        val updatedTags = updated.tags ?: emptyMap()

        val tagsToAdd = updatedTags.filter { (key, value) ->
            existingTags[key] != value
        }

        val tagsToDelete = existingTags.filter { (key, _) ->
            !updatedTags.containsKey(key) && updated.tags != null
        }

        // Update user in database
        return userRepository.update(updatedUser, tagsToAdd, tagsToDelete)
    }

    /**
     * Create/Grant a new user
     */
    fun grant(securityContext: SecurityContext, newUser: NewUserRequest): User {
        logger.debug("UserService > grant > newUser: {}", newUser)

        // Validate that the user can create users in this group
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.ADMIN,
            CheckType.ALL,
        )

        if (!isAuthorized) {
            throw UnauthorizedException("You must be an admin to grant access to users")
        }

        // Normalize email to lowercase
        val normalizedEmail = newUser.email.lowercase()

        // Check if user already exists
        val existingUser = userRepository.get(normalizedEmail)

        if (existingUser != null) {
            // User exists, check if they're already in this group
            if (existingUser.groups?.containsKey(securityContext.groupId) == true) {
                throw InvalidRequestException(
                    "User $normalizedEmail already has access to group ${securityContext.groupId}",
                )
            }

            // Add user to this group with the specified role
            val updatedGroups = existingUser.groups?.toMutableMap() ?: mutableMapOf()
            updatedGroups[securityContext.groupId] = newUser.role

            val now = Instant.now()
            val updatedUser = existingUser.copy(
                groups = updatedGroups,
                firstName = newUser.firstName ?: existingUser.firstName,
                lastName = newUser.lastName ?: existingUser.lastName,
                defaultGroup = newUser.defaultGroup ?: existingUser.defaultGroup ?: securityContext.groupId,
                tags = mergeTags(existingUser.tags, newUser.tags),
                updatedAt = now,
                updatedBy = securityContext.email,
            )

            // Calculate tag differences
            val existingTags = existingUser.tags ?: emptyMap()
            val newTags = newUser.tags ?: emptyMap()

            val tagsToAdd = newTags.filter { (key, value) ->
                existingTags[key] != value
            }

            val tagsToDelete = emptyMap<String, Any>()

            return userRepository.update(updatedUser, tagsToAdd, tagsToDelete)
        } else {
            // Create new user
            val now = Instant.now()
            val user = User(
                email = normalizedEmail,
                firstName = newUser.firstName,
                lastName = newUser.lastName,
                state = UserState.INVITED,
                groups = mapOf(securityContext.groupId to newUser.role),
                tags = newUser.tags,
                defaultGroup = newUser.defaultGroup ?: securityContext.groupId,
                createdAt = now,
                createdBy = securityContext.email,
                updatedAt = now,
                updatedBy = securityContext.email,
            )

            return userRepository.create(user)
        }
    }

    /**
     * Revoke user's access to a group
     */
    fun revoke(securityContext: SecurityContext, email: String) {
        logger.debug("UserService > revoke > email: {}", email)

        // Normalize email to lowercase
        val normalizedEmail = email.lowercase()

        // Authorization check: Only admins can revoke access
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.ADMIN,
            CheckType.ALL,
        )

        if (!isAuthorized) {
            throw UnauthorizedException("You must be an admin to revoke user access")
        }

        // Check if user exists
        val existingUser = userRepository.get(normalizedEmail)
            ?: throw ResourceNotFoundException("User with email $normalizedEmail not found")

        // Check if user is in this group
        if (existingUser.groups?.containsKey(securityContext.groupId) != true) {
            throw InvalidRequestException(
                "User $normalizedEmail does not have access to group ${securityContext.groupId}",
            )
        }

        // Cannot revoke the last admin in the root group
        if (securityContext.groupId == "/" && existingUser.groups["/"] == UserGroupRole.ADMIN) {
            val rootAdmins = userRepository.list(
                states = listOf(UserState.ACTIVE, UserState.INVITED),
            ).filter {
                it.email != normalizedEmail &&
                    it.groups?.get("/") == UserGroupRole.ADMIN
            }

            if (rootAdmins.isEmpty()) {
                throw InvalidRequestException("Cannot revoke the last admin in the root group")
            }
        }

        // Remove user from this group
        val updatedGroups = existingUser.groups.toMutableMap()
        updatedGroups.remove(securityContext.groupId)

        // If user has no groups left, delete the user
        if (updatedGroups.isEmpty()) {
            userRepository.delete(normalizedEmail)
            return
        }

        // Update user's default group if needed
        val defaultGroup = if (existingUser.defaultGroup == securityContext.groupId) {
            updatedGroups.keys.first()
        } else {
            existingUser.defaultGroup
        }

        val now = Instant.now()
        val updatedUser = existingUser.copy(
            groups = updatedGroups,
            defaultGroup = defaultGroup,
            updatedAt = now,
            updatedBy = securityContext.email,
        )

        userRepository.update(updatedUser, null, null)
    }

    /**
     * List users with pagination and filtering
     */
    fun list(
        securityContext: SecurityContext,
        options: UserListOptions? = null,
    ): Pair<List<User>, UserListPaginationKey?> {
        logger.debug("UserService > list > options: {}", options)

        // Authorization check: Must have at least reader access
        val isAuthorized = SecurityUtils.isAuthorized(
            listOf(securityContext.groupId),
            securityContext.groupRoles,
            UserGroupRole.READER,
            CheckType.ALL,
        )

        if (!isAuthorized) {
            throw UnauthorizedException("You must have at least reader access to list users")
        }

        // Get users
        val users = userRepository.list(
            limit = options?.count?.plus(1), // Add 1 for pagination detection
            fromEmail = options?.exclusiveStart?.paginationToken,
            tags = options?.tags,
            states = null, // Get all states
        )

        // Handle pagination
        val paginationKey = if (users.size > (options?.count ?: 10)) {
            val lastItem = users[options?.count ?: 10 - 1]
            UserListPaginationKey(paginationToken = lastItem.email)
        } else {
            null
        }

        // Return the correct number of items
        val resultUsers = if (users.size > (options?.count ?: 10)) {
            users.subList(0, options?.count ?: 10)
        } else {
            users
        }

        return Pair(resultUsers, paginationKey)
    }

    /**
     * Helper method to check if a group is a child or the same as any of the groups in the map
     */
    private fun isChildGroup(groups: Groups?, defaultGroup: String?): Boolean {
        if (defaultGroup == null || groups == null) {
            return false
        }

        return groups.keys.any { groupId ->
            defaultGroup == groupId || defaultGroup.startsWith("$groupId/")
        }
    }

    /**
     * Helper method to merge tags
     */
    private fun mergeTags(existingTags: Tags?, newTags: Tags?): Tags {
        if (newTags == null) {
            return existingTags ?: emptyMap()
        }

        val result = existingTags?.toMutableMap() ?: mutableMapOf()
        newTags.forEach { (key, value) -> result[key] = value }
        return result
    }
} 
