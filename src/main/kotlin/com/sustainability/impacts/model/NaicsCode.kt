package com.sustainability.impacts.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Represents a NAICS (North American Industry Classification System) code
 */
@Document(collection = "naics_codes")
data class NaicsCode(
    @Id
    val id: String? = null,
    val code: String,
    val title: String,
    val parentCode: String? = null,
    val level: Int,
    val children: List<String>? = null, // List of child code IDs
)

/**
 * Represents a nested NAICS code for UI rendering
 */
data class NaicsCodeDto(
    val code: String,
    val title: String,
    val children: List<NaicsCodeDto>? = null,
) 
