package com.sustainability.accessmanagement.auth.controller

import com.sustainability.accessmanagement.auth.TokenGenerator
import com.sustainability.config.ApiVersionConfig
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for generating authentication tokens for testing purposes.
 * Note: In a production environment, token generation would be handled
 * by a dedicated auth service.
 */
@RestController("accessManagementTokenController")
@RequestMapping(ApiVersionConfig.API_V1_AUTH)
class TokenController {

    /**
     * Request body for token generation
     */
    data class TokenRequest(
        val email: String,
        val groupId: String,
        val role: String,
    )

    /**
     * Response body for token generation
     */
    data class TokenResponse(
        val token: String,
        val email: String,
        val groupId: String,
        val role: String,
        val expiresInHours: Long = 24,
    )

    /**
     * Generates a JWT token for the specified user, group, and role
     */
    @PostMapping("/token")
    fun generateToken(@RequestBody request: TokenRequest): ResponseEntity<TokenResponse> {
        val token = TokenGenerator.createTestToken(
            email = request.email,
            groupId = request.groupId,
            role = request.role,
        )

        return ResponseEntity.ok(
            TokenResponse(
                token = token,
                email = request.email,
                groupId = request.groupId,
                role = request.role,
            ),
        )
    }

    /**
     * Generates a token specifically for Nitika as admin
     */
    @PostMapping("/admin-token")
    fun generateAdminToken(@RequestBody request: Map<String, String>): ResponseEntity<TokenResponse> {
        val groupId = request["groupId"] ?: "group-sustainability-123"
        val token = TokenGenerator.createNitikaAdminToken(groupId)

        return ResponseEntity.ok(
            TokenResponse(
                token = token,
                email = "nitika.sharma@example.com",
                groupId = groupId,
                role = "admin",
            ),
        )
    }
} 
