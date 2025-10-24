package com.sustainability.config

/**
 * Configuration constants for API versioning
 * This centralizes the API version information so it can be maintained in one place
 */
object ApiVersionConfig {
    // Base API path with version
    const val API_V1_BASE = "/api/v1"

    // Resource endpoints
    const val API_V1_USERS = "$API_V1_BASE/users"
    const val API_V1_GROUPS = "$API_V1_BASE/groups"
    const val API_V1_AUTH = "$API_V1_BASE/access-management/auth"

    // For future versions
    // const val API_V2_BASE = "/api/v2"
} 
