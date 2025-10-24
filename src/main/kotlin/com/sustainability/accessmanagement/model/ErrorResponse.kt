package com.sustainability.accessmanagement.model

import java.time.Instant

/**
 * Standard error response format
 */
data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
) 
