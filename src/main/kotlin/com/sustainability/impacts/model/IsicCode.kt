package com.sustainability.impacts.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Represents an ISIC (International Standard Industrial Classification) code
 */
@Document(collection = "isic_codes")
data class IsicCode(
    @Id
    val id: String? = null,
    val code: String,
    val title: String,
    val section: String? = null,
    val division: String? = null,
    val group: String? = null,
    val revision: String = "4", // Default to Revision 4
)

/**
 * DTO for ISIC code responses
 */
data class IsicCodeDto(
    val code: String,
    val title: String,
    val section: String? = null,
    val division: String? = null,
    val group: String? = null,
    val revision: String = "4",
) 
