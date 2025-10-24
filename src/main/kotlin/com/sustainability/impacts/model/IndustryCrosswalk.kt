package com.sustainability.impacts.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Represents a crosswalk mapping between different industry classification systems.
 * This allows for looking up equivalent codes across systems.
 */
@Document(collection = "industry_crosswalk")
data class IndustryCrosswalk(
    @Id
    val id: String? = null,
    val naicsCode: String,
    val sicCode: String? = null,
    val isicCode: String? = null,
    val naicsYear: String = "2022", // Default to 2022 NAICS
    val sicYear: String = "1987", // Default to 1987 SIC
    val isicRevision: String = "4", // Default to ISIC Rev. 4
)

/**
 * DTO for crosswalk suggestions based on a primary code
 */
data class CrosswalkSuggestionDto(
    val naicsCode: String? = null,
    val naicsTitle: String? = null,
    val sicCode: String? = null,
    val sicTitle: String? = null,
    val isicCode: String? = null,
    val isicTitle: String? = null,
) 
