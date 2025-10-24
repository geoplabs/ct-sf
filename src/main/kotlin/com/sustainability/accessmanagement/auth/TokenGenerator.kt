package com.sustainability.accessmanagement.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Utility class for generating JWT tokens for testing purposes.
 * This is NOT meant for production use but rather for developers
 * to test the application API endpoints.
 */
object TokenGenerator {
    // This is a test key, never use hardcoded keys in production!
    // Using the same key as JwtAuthFilter to ensure signatures match
    private val SECRET_KEY = Keys.hmacShaKeyFor(
        "this-is-a-testing-key-that-should-be-very-long-and-secure".toByteArray(),
    )

    /**
     * Generates a JWT token for a user with specified claims.
     *
     * @param userId User ID
     * @param email User email
     * @param groupId Primary group ID the user belongs to
     * @param expirationHours Token expiration in hours
     * @return JWT token string
     */
    fun generateToken(userId: String, email: String, groupId: String, expirationHours: Long = 24): String {
        val now = Instant.now()
        val expiration = now.plus(expirationHours, ChronoUnit.HOURS)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .claim("email", email)
            .claim("groupId", groupId)
            .claim("roles", listOf("READER")) // Default to reader role
            .signWith(SECRET_KEY)
            .compact()
    }

    /**
     * Generates a JWT token for a specific user and group with assigned roles.
     *
     * @param userId User ID
     * @param email User email
     * @param groupId Group ID for this token context
     * @param roles List of roles the user has in the specified group
     * @param expirationHours Token expiration in hours
     * @return JWT token string
     */
    fun generateTokenWithRoles(
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
     * Creates a sample JWT token for "nitika.sharma@example.com" with admin role.
     *
     * @param groupId The group ID to use in the token
     * @return JWT token string
     */
    fun createNitikaAdminToken(groupId: String): String {
        return generateTokenWithRoles(
            userId = "user-nitika-123",
            email = "nitika.sharma@example.com",
            groupId = groupId,
            roles = listOf("admin"),
        )
    }

    /**
     * Creates a sample token with the specified role for testing
     *
     * @param email User email
     * @param groupId Group ID
     * @param role Role to assign (reader, contributor, admin)
     * @return JWT token string
     */
    fun createTestToken(email: String, groupId: String, role: String): String {
        val userId = "user-${UUID.randomUUID()}"
        return generateTokenWithRoles(
            userId = userId,
            email = email,
            groupId = groupId,
            roles = listOf(role),
        )
    }
} 
