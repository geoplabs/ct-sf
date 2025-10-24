package com.sustainability.accessmanagement.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sustainability.accessmanagement.model.EditUserRequest
import com.sustainability.accessmanagement.model.NewUserRequest
import com.sustainability.accessmanagement.model.PaginationResponse
import com.sustainability.accessmanagement.model.SecurityContext
import com.sustainability.accessmanagement.model.Tags
import com.sustainability.accessmanagement.model.User
import com.sustainability.accessmanagement.model.UserGroupRole
import com.sustainability.accessmanagement.model.UserListOptions
import com.sustainability.accessmanagement.model.UserListPaginationKey
import com.sustainability.accessmanagement.model.UserListResponse
import com.sustainability.accessmanagement.service.UserService
import com.sustainability.config.ApiVersionConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping(ApiVersionConfig.API_V1_USERS)
class UserController(
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)

    /**
     * Get a user by email
     */
    @GetMapping("/{encodedEmail}")
    fun getUser(@PathVariable encodedEmail: String, request: HttpServletRequest): ResponseEntity<User> {
        val email = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())
        val securityContext = getSecurityContext(request)

        val user = userService.get(securityContext, email)
        return ResponseEntity.ok(user)
    }

    /**
     * List users with optional pagination and filtering
     */
    @GetMapping
    fun listUsers(
        @RequestParam(required = false) fromEmail: String?,
        @RequestParam(required = false) count: Int?,
        // Tags would be passed as request params like: tags.key1=value1&tags.key2=value2
        request: HttpServletRequest,
    ): ResponseEntity<UserListResponse> {
        val securityContext = getSecurityContext(request)

        // Extract tags from request parameters
        val tags = extractTags(request)

        val options = UserListOptions(
            count = count ?: 10,
            exclusiveStart = fromEmail?.let { UserListPaginationKey(it) },
            tags = if (tags.isEmpty()) null else tags,
            includeChildGroups = true,
        )

        val (users, paginationKey) = userService.list(securityContext, options)

        val response = UserListResponse(
            users = users,
            pagination = paginationKey?.let { PaginationResponse(it.paginationToken) },
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Grant a user access to the current group
     */
    @PostMapping
    fun grantUser(@RequestBody newUser: NewUserRequest, request: HttpServletRequest): ResponseEntity<User> {
        val securityContext = getSecurityContext(request)

        val user = userService.grant(securityContext, newUser)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    /**
     * Revoke a user's access to the current group
     */
    @DeleteMapping("/{encodedEmail}")
    fun revokeUser(
        @PathVariable encodedEmail: String,
        request: HttpServletRequest,
    ): ResponseEntity<Map<String, String>> {
        val email = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())
        val securityContext = getSecurityContext(request)

        userService.revoke(securityContext, email)

        // Return a success response
        val response = mapOf("message" to "User access successfully revoked", "email" to email)
        return ResponseEntity.ok(response)
    }

    /**
     * Update a user
     */
    @PatchMapping("/{encodedEmail}")
    fun updateUser(
        @PathVariable encodedEmail: String,
        @RequestBody editUser: EditUserRequest,
        request: HttpServletRequest,
    ): ResponseEntity<User> {
        val email = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())
        val securityContext = getSecurityContext(request)

        val updatedUser = userService.update(securityContext, email, editUser)
        return ResponseEntity.ok(updatedUser)
    }

    /**
     * Helper method to get the security context from the request
     * In a real implementation, this would be extracted from authentication token/session
     */
    private fun getSecurityContext(request: HttpServletRequest): SecurityContext {
        // In a real implementation, this would extract information from the JWT token
        // or other authentication mechanism

        // For now, we'll use a mock security context
        // The actual implementation would depend on your authentication setup
        val groupId = request.getHeader("X-Group-Context") ?: "root"
        val email = request.getHeader("X-User-Email") ?: "admin@example.com"

        // In a real implementation, these roles would come from the authenticated user's session
        val groupRoles = mapOf(
            "root" to UserGroupRole.ADMIN,
            groupId to UserGroupRole.ADMIN,
        )

        return SecurityContext(
            email = email,
            groupId = groupId,
            groupRoles = groupRoles,
        )
    }

    /**
     * Helper method to extract tags from request parameters
     * Enhanced to handle complex object values and nested paths
     */
    private fun extractTags(request: HttpServletRequest): Tags {
        val tags = mutableMapOf<String, Any>()
        val paramNames = request.parameterNames

        while (paramNames.hasMoreElements()) {
            val paramName = paramNames.nextElement()
            if (paramName.startsWith("tags.")) {
                val tagKey = paramName.substring(5) // Remove "tags." prefix
                val tagValue = request.getParameter(paramName)

                // Try to parse as JSON if it looks like a JSON object or array
                if ((tagValue.startsWith("{") && tagValue.endsWith("}")) ||
                    (tagValue.startsWith("[") && tagValue.endsWith("]"))
                ) {
                    try {
                        // Parse as JSON and convert to appropriate object
                        val jsonValue = objectMapper.readValue(tagValue, Any::class.java)
                        addNestedValue(tags, tagKey, jsonValue)
                    } catch (e: Exception) {
                        logger.warn("Failed to parse JSON value for tag {}: {}", tagKey, e.message)
                        // If parsing fails, use as string
                        addNestedValue(tags, tagKey, tagValue)
                    }
                } else {
                    // Handle primitive values intelligently
                    val processedValue = when {
                        tagValue.equals("true", ignoreCase = true) -> true
                        tagValue.equals("false", ignoreCase = true) -> false
                        tagValue.toIntOrNull() != null -> tagValue.toInt()
                        tagValue.toDoubleOrNull() != null -> tagValue.toDouble()
                        else -> tagValue
                    }
                    addNestedValue(tags, tagKey, processedValue)
                }
            }
        }

        return tags
    }

    /**
     * Helper method to handle nested tag paths (e.g., "parent.child.grandchild")
     */
    @Suppress("UNCHECKED_CAST")
    private fun addNestedValue(tags: MutableMap<String, Any>, path: String, value: Any) {
        val parts = path.split(".")

        if (parts.size == 1) {
            // Simple case: direct key
            tags[path] = value
            return
        }

        // Handle nested paths
        var current = tags
        for (i in 0 until parts.size - 1) {
            val key = parts[i]
            if (!current.containsKey(key)) {
                current[key] = mutableMapOf<String, Any>()
            } else if (current[key] !is MutableMap<*, *>) {
                // Convert to map if it's not already one
                current[key] = mutableMapOf<String, Any>()
            }
            current = current[key] as MutableMap<String, Any>
        }

        // Set the final value
        current[parts.last()] = value
    }
} 
