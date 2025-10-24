package com.sustainability.impacts.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Represents a SIC (Standard Industry Classification) code
 */
@Document(collection = "sic_codes")
data class SicCode(
    @Id
    val id: String? = null,
    val code: String,
    val title: String,
    val division: String? = null,
    val majorGroup: String? = null,
)

/**
 * DTO for SIC code responses
 */
data class SicCodeDto(
    val code: String,
    val title: String,
    val division: String? = null,
    val majorGroup: String? = null,
) 
