package com.sustainability.accessmanagement.model

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Model classes for complex tag structures used in the application
 *
 * IMPORTANT: These models serve primarily as reference documentation for expected structures.
 * The actual API implementation uses a dynamic approach with Map<String, Any> to support any JSON structure.
 * These models can be used for validation, documentation, and type-safe operations when needed,
 * but the application doesn't require JSON to strictly follow these structures.
 */

/**
 * Base dynamic object that can hold any properties
 * This allows for flexibility in JSON structures
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class DynamicProperties {
    private val properties: MutableMap<String, Any?> = mutableMapOf()

    @JsonAnySetter
    fun set(name: String, value: Any?) {
        properties[name] = value
    }

    @JsonAnyGetter
    fun properties(): Map<String, Any?> = properties

    fun getProperty(name: String): Any? = properties[name]
}

/**
 * Wrapper for simple values with type safety
 */
data class TagValue<T>(
    val value: T,
)

/**
 * Industry classification codes structure
 * Example of a strongly typed model that can be used for validation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class IndustryClassificationCodes(
    val naics: List<TagValue<String>>? = null,
    val sic: List<TagValue<String>>? = null,
    val isic: List<TagValue<String>>? = null,
    val gics: List<TagValue<String>>? = null,
    val nace: List<TagValue<String>>? = null,
) : DynamicProperties()

/**
 * Primary industry classification
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PrimaryIndustryClassification(
    val taxonomy: TagValue<String>? = null,
    val code: TagValue<String>? = null,
) : DynamicProperties()

/**
 * Complete industry classification structure
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class IndustryClassification(
    val codes: IndustryClassificationCodes? = null,
    val primary: PrimaryIndustryClassification? = null,
) : DynamicProperties()

/**
 * Region with countries
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OperationRegion(
    val name: TagValue<String>? = null,
    val countries: List<TagValue<String>>? = null,
) : DynamicProperties()

/**
 * Headquarters location information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class HeadquartersLocation(
    val streetAddress: TagValue<String>? = null,
    val city: TagValue<String>? = null,
    val country: TagValue<String>? = null,
    val latitude: TagValue<String>? = null,
    val longitude: TagValue<String>? = null,
) : DynamicProperties()

/**
 * Operations information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Operations(
    val regions: List<OperationRegion>? = null,
    val headquarters: HeadquartersLocation? = null,
) : DynamicProperties()

/**
 * Custom fiscal year dates
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CustomFiscalYear(
    val start: TagValue<String?>? = null,
    val end: TagValue<String?>? = null,
) : DynamicProperties()

/**
 * Reporting and baseline information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReportingBaseline(
    val useCalendarYear: TagValue<Boolean>? = null,
    val customFiscalYear: CustomFiscalYear? = null,
    val baselineYear: TagValue<Int>? = null,
    val baselineAdjustmentNotes: TagValue<String>? = null,
    val unitSystem: TagValue<String>? = null,
    val gwpHorizon: TagValue<String>? = null,
) : DynamicProperties()

/**
 * Target commitments information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TargetsCommitments(
    val netZeroCommitted: TagValue<Boolean>? = null,
    val sbtiCommitted: TagValue<Boolean>? = null,
    val targetType: TagValue<String>? = null,
    val targetYear: TagValue<Int>? = null,
    val reductionPercentage: TagValue<Int>? = null,
) : DynamicProperties()

/**
 * Contact person information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ContactPerson(
    val name: TagValue<String>? = null,
    val email: TagValue<String>? = null,
    val phone: TagValue<String>? = null,
) : DynamicProperties()

/**
 * Governance structure
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Governance(
    val sustainabilityLead: ContactPerson? = null,
    val financeLead: ContactPerson? = null,
    val boardOversight: TagValue<Boolean>? = null,
) : DynamicProperties()

/**
 * Security preferences
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SecurityPreferences(
    val ssoDomains: List<TagValue<String>>? = null,
    val dataResidencyRegion: TagValue<String>? = null,
    val adminUsers: List<TagValue<String>>? = null,
) : DynamicProperties()

/**
 * Legal consent information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LegalConsent(
    val dpaAccepted: TagValue<Boolean>? = null,
    val retentionPeriodYears: TagValue<Int>? = null,
) : DynamicProperties()

/**
 * Main company tag structure that can be used to strongly type the tags
 * This can be used for validation and type-safe operations on the tags when needed
 *
 * IMPORTANT: While this structure documents the expected fields, the actual API
 * will accept any valid JSON structure as tags, not limited to what's defined here.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CompanyTags(
    val industryClassification: IndustryClassification? = null,
    val businessDescription: TagValue<String>? = null,
    val operations: Operations? = null,
    val reportingBaseline: ReportingBaseline? = null,
    val targetsCommitments: TargetsCommitments? = null,
    val governance: Governance? = null,
    val securityPreferences: SecurityPreferences? = null,
    val legalConsent: LegalConsent? = null,
    val priority: TagValue<String>? = null,
) : DynamicProperties()

/**
 * Utility functions for working with tag structures
 */
object TagUtils {
    /**
     * Converts a generic Map<String, Any> to a typed CompanyTags object
     * This can be used when validation or type-safe operations are needed
     */
    fun mapToCompanyTags(tags: Map<String, Any>): CompanyTags {
        val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        return objectMapper.convertValue(tags, CompanyTags::class.java)
    }

    /**
     * Converts a typed CompanyTags object back to a generic Map<String, Any>
     */
    fun companyTagsToMap(tags: CompanyTags): Map<String, Any> {
        val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        return objectMapper.convertValue(tags, Map::class.java) as Map<String, Any>
    }

    /**
     * Validates if a Map<String, Any> conforms to the CompanyTags structure
     * Returns a list of validation errors, empty if valid
     */
    fun validateTags(tags: Map<String, Any>): List<String> {
        val errors = mutableListOf<String>()
        try {
            mapToCompanyTags(tags)
        } catch (e: Exception) {
            errors.add("Invalid tag structure: ${e.message}")
        }
        return errors
    }
} 
