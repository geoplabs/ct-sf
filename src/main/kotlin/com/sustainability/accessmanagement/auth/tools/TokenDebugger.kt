package com.sustainability.accessmanagement.auth.tools

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Utility class for debugging JWT tokens in development.
 * Provides methods to generate compatible tokens and debug existing ones.
 */
object TokenDebugger {
    // Using the same key as TokenGenerator and JwtAuthFilter to ensure compatibility
    private val SECRET_KEY = Keys.hmacShaKeyFor(
        "this-is-a-testing-key-that-should-be-very-long-and-secure".toByteArray(),
    )

    /**
     * Generates a JWT token compatible with the application's authentication system.
     *
     * @param userId User ID
     * @param email User email
     * @param groupId Group ID for this token context
     * @param roles List of roles the user has in the specified group
     * @param expirationHours Token expiration in hours
     * @return JWT token string
     */
    fun generateCompatibleToken(
        userId: String,
        email: String,
        groupId: String,
        roles: List<String>,
        expirationHours: Long = 24,
    ): String {
        val now = Instant.now()
        val expiration = now.plus(expirationHours, ChronoUnit.HOURS)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .claim("email", email)
            .claim("groupId", groupId)
            .claim("roles", roles)
            .signWith(SECRET_KEY)
            .compact()
    }

    /**
     * Debugs a JWT token by parsing it and returning its contents.
     *
     * @param token JWT token string
     * @return Map containing token information or error details
     */
    fun debugToken(token: String): Map<String, Any> {
        return try {
            val claims = parseToken(token)

            val issuedAt = claims.issuedAt
            val expiration = claims.expiration
            val now = Date()

            mapOf(
                "valid" to true,
                "subject" to (claims.subject ?: "No subject"),
                "email" to (claims.get("email", String::class.java) ?: "No email"),
                "groupId" to (claims.get("groupId", String::class.java) ?: "No groupId"),
                "roles" to (claims.get("roles") ?: "No roles"),
                "issuedAt" to (issuedAt?.toString() ?: "No issued date"),
                "expiration" to (expiration?.toString() ?: "No expiration"),
                "expired" to (expiration?.before(now) ?: false),
                "timeUntilExpiration" to if (expiration != null) {
                    "${(expiration.time - now.time) / (1000 * 60)} minutes"
                } else {
                    "Unknown"
                },
                "allClaims" to claims.entries.associate { it.key to it.value },
            )
        } catch (ex: Exception) {
            mapOf(
                "valid" to false,
                "error" to (ex.message ?: "Unknown error"),
                "errorType" to ex.javaClass.simpleName,
                "suggestion" to when {
                    ex.message?.contains("expired") == true -> "Token has expired. Generate a new one."
                    ex.message?.contains(
                        "signature",
                    ) == true -> "Invalid signature. Make sure you're using the correct token."
                    ex.message?.contains(
                        "malformed",
                    ) == true -> "Token format is invalid. Check if the token is complete."
                    else -> "Check if the token is valid and properly formatted."
                },
            )
        }
    }

    /**
     * Parse and validate the JWT token using the same logic as JwtAuthFilter
     */
    private fun parseToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .body
    }

    /**
     * Generates a quick test token with admin privileges for debugging
     */
    fun generateQuickAdminToken(groupId: String = "debug-group-123"): String {
        return generateCompatibleToken(
            userId = "debug-admin-user",
            email = "debug@example.com",
            groupId = groupId,
            roles = listOf("admin"),
            expirationHours = 24,
        )
    }

    /**
     * Generates a quick test token with reader privileges for debugging
     */
    fun generateQuickReaderToken(groupId: String = "debug-group-123"): String {
        return generateCompatibleToken(
            userId = "debug-reader-user",
            email = "reader@example.com",
            groupId = groupId,
            roles = listOf("reader"),
            expirationHours = 24,
        )
    }
}
