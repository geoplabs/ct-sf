package com.sustainability.accessmanagement.auth

import com.sustainability.accessmanagement.model.UserGroupRole
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import com.sustainability.accessmanagement.model.SecurityContext as AccessSecurityContext
import com.sustainability.impacts.security.SecurityContext as ImpactsSecurityContext

/**
 * Filter that adapts between different SecurityContext implementations.
 * This solves the type conversion issue between access management and impacts modules.
 */
@Component("accessManagementSecurityContextAdapter")
@Order(2) // Execute this filter after JwtAuthFilter
class SecurityContextAdapter : Filter {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest

        logger.info("SecurityContextAdapter processing request to: ${httpRequest.requestURI}")

        try {
            // Get the security context from JwtAuthFilter
            val accessSecurityContext = httpRequest.getAttribute(
                JwtAuthFilter.SECURITY_CONTEXT_ATTR,
            ) as? AccessSecurityContext

            if (accessSecurityContext == null) {
                logger.warn(
                    "No AccessSecurityContext found in request attribute: ${JwtAuthFilter.SECURITY_CONTEXT_ATTR}",
                )
            } else {
                logger.info("Found AccessSecurityContext: $accessSecurityContext")

                // Convert to the Impacts SecurityContext
                val impactsSecurityContext = convertToImpactsSecurityContext(accessSecurityContext)
                logger.info("Converted to ImpactsSecurityContext: $impactsSecurityContext")

                // Replace the security context in the request
                httpRequest.setAttribute(JwtAuthFilter.SECURITY_CONTEXT_ATTR, impactsSecurityContext)
                logger.info("Successfully set ImpactsSecurityContext in request attribute")
            }
        } catch (ex: Exception) {
            logger.error("Error converting SecurityContext: ${ex.message}", ex)
        }

        // Continue with the filter chain
        chain.doFilter(request, response)
    }

    /**
     * Converts from AccessManagement SecurityContext to Impacts SecurityContext
     */
    private fun convertToImpactsSecurityContext(accessContext: AccessSecurityContext): ImpactsSecurityContext {
        // Extract the role for the current group
        val groupRole = accessContext.groupRoles[accessContext.groupId] ?: UserGroupRole.READER
        logger.debug("Extracted role for group ${accessContext.groupId}: $groupRole")

        // Map UserGroupRole enum to string role
        val roleString = when (groupRole) {
            UserGroupRole.ADMIN -> "admin"
            UserGroupRole.CONTRIBUTOR -> "contributor"
            UserGroupRole.READER -> "reader"
        }

        val userId = extractUserIdFromEmail(accessContext.email)
        return ImpactsSecurityContext(
            userId = userId,
            groupId = accessContext.groupId,
            groupRoles = listOf(roleString),
        )
    }

    /**
     * Extracts a userId from an email address
     */
    private fun extractUserIdFromEmail(email: String): String {
        return "user-${email.substringBefore("@").replace(".", "-")}"
    }
} 
