package com.sustainability.accessmanagement.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sustainability.accessmanagement.model.EditGroupRequest
import com.sustainability.accessmanagement.model.Group
import com.sustainability.accessmanagement.model.GroupListOptions
import com.sustainability.accessmanagement.model.GroupListPaginationKey
import com.sustainability.accessmanagement.model.GroupListResponse
import com.sustainability.accessmanagement.model.NewGroupRequest
import com.sustainability.accessmanagement.model.PaginationResponse
import com.sustainability.accessmanagement.model.SecurityContext
import com.sustainability.accessmanagement.model.TagUtils
import com.sustainability.accessmanagement.model.Tags
import com.sustainability.accessmanagement.model.UserGroupRole
import com.sustainability.accessmanagement.service.GroupService
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

@RestController
@RequestMapping(ApiVersionConfig.API_V1_GROUPS)
class GroupController(
    private val groupService: GroupService,
    private val objectMapper: ObjectMapper, // Inject ObjectMapper for JSON handling
) {
    private val logger = LoggerFactory.getLogger(GroupController::class.java)

    /**
     * Get a group by ID
     */
    @GetMapping("/{encodedGroupId}")
    fun getGroup(
        @PathVariable encodedGroupId: String,
        @RequestParam(required = false, defaultValue = "false") showConfigurationSource: Boolean,
        request: HttpServletRequest,
    ): ResponseEntity<Group> {
        val securityContext = getSecurityContext(request)
        // Add leading slash if not present to match MongoDB format
        val groupId = if (encodedGroupId.startsWith("/")) encodedGroupId else "/$encodedGroupId"
        val group = groupService.get(securityContext, groupId, showConfigurationSource)
        return ResponseEntity.ok(group)
    }

    /**
     * List groups with optional pagination and filtering
     */
    @GetMapping
    fun listGroups(
        @RequestParam(required = false) fromGroupId: String?,
        @RequestParam(required = false) count: Int?,
        @RequestParam(required = false, defaultValue = "false") includeChildGroups: Boolean,
        @RequestParam(required = false, defaultValue = "false") includeParentGroups: Boolean,
        @RequestParam(required = false) accountId: String?,
        // Tags would be passed as request params like: tags.key1=value1&tags.key2=value2
        request: HttpServletRequest,
    ): ResponseEntity<GroupListResponse> {
        val securityContext = getSecurityContext(request)

        // Extract tags from request parameters
        val tags = extractTags(request)

        val options = GroupListOptions(
            count = count ?: 10,
            exclusiveStart = fromGroupId?.let { GroupListPaginationKey(it) },
            tags = if (tags.isEmpty()) null else tags,
            accountId = accountId,
            includeChildGroups = includeChildGroups,
            includeParentGroups = includeParentGroups,
        )

        val (groups, paginationKey) = groupService.list(securityContext, options)

        val response = GroupListResponse(
            groups = groups,
            pagination = paginationKey?.let { PaginationResponse(it.paginationToken) },
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Create a new group
     */
    @PostMapping
    fun createGroup(@RequestBody newGroup: NewGroupRequest, request: HttpServletRequest): ResponseEntity<Group> {
        val securityContext = getSecurityContext(request)

        // Log the incoming request for debugging
        if (logger.isDebugEnabled) {
            logger.debug("Creating new group with tags: {}", newGroup.tags?.let { objectMapper.writeValueAsString(it) })
        }

        val group = groupService.create(securityContext, newGroup)
        return ResponseEntity.status(HttpStatus.CREATED).body(group)
    }

    /**
     * Update a group
     */
    @PatchMapping("/{encodedGroupId}")
    fun updateGroup(
        @PathVariable encodedGroupId: String,
        @RequestBody editGroup: EditGroupRequest,
        request: HttpServletRequest,
    ): ResponseEntity<Group> {
        val securityContext = getSecurityContext(request)

        // Log the incoming request for debugging
        if (logger.isDebugEnabled) {
            try {
                logger.debug(
                    "Updating group {} with tags: {}",
                    encodedGroupId,
                    editGroup.tags?.let { objectMapper.writeValueAsString(it) },
                )

                // Optionally validate the tag structure without enforcing it
                editGroup.tags?.let { tags ->
                    val validationErrors = TagUtils.validateTags(tags)
                    if (validationErrors.isNotEmpty()) {
                        logger.warn("Tag structure has potential issues: {}", validationErrors)
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error serializing request tags for logging: {}", e.message)
            }
        }

        // Add leading slash if not present to match MongoDB format
        val groupId = if (encodedGroupId.startsWith("/")) encodedGroupId else "/$encodedGroupId"
        val updatedGroup = groupService.update(securityContext, groupId, editGroup)
        return ResponseEntity.ok(updatedGroup)
    }

    /**
     * Delete a group
     */
    @DeleteMapping("/{encodedGroupId}")
    fun deleteGroup(
        @PathVariable encodedGroupId: String,
        request: HttpServletRequest,
    ): ResponseEntity<Map<String, String>> {
        val securityContext = getSecurityContext(request)

        // Add leading slash if not present to match MongoDB format
        val groupId = if (encodedGroupId.startsWith("/")) encodedGroupId else "/$encodedGroupId"
        groupService.delete(securityContext, groupId)

        // Return a success response
        val response = mapOf("message" to "Group successfully deleted", "groupId" to groupId)
        return ResponseEntity.ok(response)
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
