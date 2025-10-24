package com.sustainability.accessmanagement.auth

import com.sustainability.accessmanagement.model.SecurityContext
import com.sustainability.accessmanagement.model.UserGroupRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.security.Key

/**
 * Filter that processes JWT tokens and sets up the security context.
 * This is a reusable component that can be used across different modules.
 */
@Component
@Order(1)
class JwtAuthFilter : Filter {
    private val logger = LoggerFactory.getLogger(javaClass)

    // In a real application, this would be securely stored and not hardcoded
    private val secretKey: Key = Keys.hmacShaKeyFor(
        "this-is-a-testing-key-that-should-be-very-long-and-secure".toByteArray(),
    )

    companion object {
        const val BEARER_PREFIX = "Bearer "
        const val AUTHORIZATION_HEADER = "Authorization"
        const val GROUP_ID_HEADER = "X-Group-Id"
        const val SECURITY_CONTEXT_ATTR = "securityContext"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        try {
            // Skip auth for OPTIONS requests (CORS preflight)
            if (httpRequest.method == "OPTIONS") {
                chain.doFilter(request, response)
                return
            }

            // Attempt to extract the JWT token
            val authHeader = httpRequest.getHeader(AUTHORIZATION_HEADER)
            val groupIdHeader = httpRequest.getHeader(GROUP_ID_HEADER)

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX) && groupIdHeader != null) {
                val token = authHeader.substring(BEARER_PREFIX.length)

                // Parse and validate the token
                val claims = parseToken(token)

                // Extract user information
                val userId = claims.subject
                val email = claims.get("email", String::class.java)
                val rolesArray = when (val rolesObj = claims.get("roles")) {
                    is List<*> -> rolesObj.filterIsInstance<String>()
                    else -> listOf("READER") // Default to reader if roles not specified
                }

                // Map string roles to UserGroupRole enum values
                val groupRoles = mapOf(
                    groupIdHeader to mapRoleStringToEnum(rolesArray.firstOrNull() ?: "READER"),
                )

                // Create the security context
                val securityContext = SecurityContext(
                    email = email,
                    groupId = groupIdHeader,
                    groupRoles = groupRoles,
                    scope = null, // Can be determined based on role if needed
                )

                // Set the security context in the request
                httpRequest.setAttribute(SECURITY_CONTEXT_ATTR, securityContext)

                logger.debug(
                    "Successfully authenticated user: $email with roles: $rolesArray for group: $groupIdHeader",
                )
            } else {
                logger.debug("No authentication information provided, proceeding without security context")
            }

            // Continue with the chain
            chain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.error("Authentication failed: ${ex.message}")
            httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpResponse.writer.write("{\"error\": \"Authentication failed: ${ex.message}\"}")
        }
    }

    /**
     * Parse and validate the JWT token
     */
    private fun parseToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    /**
     * Maps a string role to UserGroupRole enum value
     */
    private fun mapRoleStringToEnum(role: String): UserGroupRole {
        return when (role.uppercase()) {
            "ADMIN" -> UserGroupRole.ADMIN
            "CONTRIBUTOR" -> UserGroupRole.CONTRIBUTOR
            else -> UserGroupRole.READER
        }
    }
} 
