package com.sustainability.accessmanagement.auth.controller

import com.sustainability.accessmanagement.auth.tools.TokenDebugger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for debugging JWT token issues.Controller with endpoints for debugging JWT tokens
 * Controller with endpoints for debugging JWT tokens
 * This controller provides endpoints for generating and validating tokens.
 */
@RestController
@RequestMapping("/jwt-debug")
class JwtDebugController {

    data class DebugTokenRequest(
        val token: String,
    )

    data class GenerateTokenRequest(
        val userId: String = "user-debug-123",
        val email: String = "debug@example.com",
        val groupId: String = "group-debug-123",
        val roles: List<String> = listOf("admin"),
        val expirationHours: Long = 24,
    )

    /**
     * Generates a guaranteed-valid JWT token for testing
     */
    @PostMapping("/generate")
    fun generateToken(
        @RequestBody request: GenerateTokenRequest = GenerateTokenRequest(),
    ): ResponseEntity<Map<String, Any>> {
        val token = TokenDebugger.generateCompatibleToken(
            userId = request.userId,
            email = request.email,
            groupId = request.groupId,
            roles = request.roles,
            expirationHours = request.expirationHours,
        )

        return ResponseEntity.ok(
            mapOf(
                "token" to token,
                "curlExample" to "curl -H \"Authorization: Bearer $token\" -H \"X-Group-Id: ${request.groupId}\" " +
                    "http://localhost:8080/your-api-endpoint",
            ),
        )
    }

    /**
     * Validates and shows the contents of a token
     */
    @PostMapping("/validate")
    fun validateToken(@RequestBody request: DebugTokenRequest): ResponseEntity<Map<String, Any>> {
        val result = TokenDebugger.debugToken(request.token)
        return ResponseEntity.ok(result)
    }

    /**
     * Simple endpoint for testing if the token is being processed correctly
     */
    @GetMapping("/test-auth")
    fun testAuth(@RequestAttribute("securityContext") securityContext: Any?): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "message" to "Authentication successful!",
                "securityContext" to (securityContext?.toString() ?: "No security context found"),
            ),
        )
    }
} 
