package com.sustainability.impacts.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.sustainability.impacts.model.NaicsCode
import com.sustainability.impacts.model.NaicsCodeDto
import com.sustainability.impacts.repository.NaicsCodeRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class NaicsCodeService @Autowired constructor(
    private val naicsCodeRepository: NaicsCodeRepository,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(NaicsCodeService::class.java)

    /**
     * Initialize NAICS codes from JSON file if the database is empty
     */
    @PostConstruct
    @Transactional
    fun initNaicsCodes() {
        if (naicsCodeRepository.count() == 0L) {
            val resource = ClassPathResource("static/data/naics_nested.json")
            val naicsTree: List<NaicsCodeDto> = objectMapper.readValue(
                resource.inputStream,
                object : TypeReference<List<NaicsCodeDto>>() {},
            )

            // Convert the nested structure to flat structure for MongoDB
            val flatNaicsCodes = flattenNaicsCodes(naicsTree)
            naicsCodeRepository.saveAll(flatNaicsCodes)
        }

        // Add sample NAICS code 11111 if it doesn't exist
        val sampleCode = "11111"
        if (naicsCodeRepository.findByCode(sampleCode) == null) {
            logger.info("Adding sample NAICS code for testing: {}", sampleCode)
            val naicsCode = NaicsCode(
                id = UUID.randomUUID().toString(),
                code = sampleCode,
                title = "Soybean Farming",
                parentCode = "1111",
                level = 5,
                children = null,
            )
            naicsCodeRepository.save(naicsCode)
        }
    }

    /**
     * Convert nested NAICS code structure to flat structure for database storage
     */
    private fun flattenNaicsCodes(
        naicsCodes: List<NaicsCodeDto>,
        parentCode: String? = null,
        level: Int = 1,
    ): List<NaicsCode> {
        val result = mutableListOf<NaicsCode>()

        for (naicsDto in naicsCodes) {
            val naicsCode = NaicsCode(
                id = UUID.randomUUID().toString(),
                code = naicsDto.code,
                title = naicsDto.title,
                parentCode = parentCode,
                level = level,
                children = naicsDto.children?.map { it.code },
            )

            result.add(naicsCode)

            // Recursively add children
            naicsDto.children?.let {
                result.addAll(flattenNaicsCodes(it, naicsCode.code, level + 1))
            }
        }

        return result
    }

    /**
     * Get all top-level NAICS codes
     */
    fun getTopLevelCodes(): List<NaicsCode> {
        return naicsCodeRepository.findByLevel(1)
    }

    /**
     * Get child NAICS codes for a parent code
     */
    fun getChildCodes(parentCode: String): List<NaicsCode> {
        return naicsCodeRepository.findByParentCode(parentCode)
    }

    /**
     * Get a NAICS code by its code value
     */
    fun getByCode(code: String): NaicsCode? {
        return naicsCodeRepository.findByCode(code)
    }

    /**
     * Get the full hierarchy for a NAICS code
     */
    fun getHierarchy(code: String): List<NaicsCode> {
        val result = mutableListOf<NaicsCode>()
        var currentCode = getByCode(code)

        while (currentCode != null) {
            result.add(0, currentCode) // Add to beginning of list
            currentCode = currentCode.parentCode?.let { getByCode(it) }
        }

        return result
    }

    /**
     * Build a nested representation of NAICS codes for the UI
     */
    fun buildNestedCodes(): List<NaicsCodeDto> {
        val topLevel = getTopLevelCodes()
        return topLevel.map { buildNestedCode(it) }
    }

    /**
     * Recursively build nested representation of a NAICS code
     */
    private fun buildNestedCode(naicsCode: NaicsCode): NaicsCodeDto {
        val childCodes = naicsCodeRepository.findByParentCode(naicsCode.code)
        val childDtos = childCodes.map { buildNestedCode(it) }

        return NaicsCodeDto(
            code = naicsCode.code,
            title = naicsCode.title,
            children = if (childDtos.isEmpty()) null else childDtos,
        )
    }

    /**
     * Search NAICS codes by title
     */
    fun searchByTitle(searchTerm: String): List<NaicsCode> {
        logger.info("Searching NAICS codes by title: {}", searchTerm)
        return naicsCodeRepository.searchByTitle(searchTerm)
    }

    /**
     * Advanced search for NAICS codes by code or title
     */
    fun searchByCodeOrTitle(searchTerm: String): List<NaicsCode> {
        logger.info("Searching NAICS codes by code or title: {}", searchTerm)

        // First try exact code match
        val exactMatch = naicsCodeRepository.findByCode(searchTerm)
        if (exactMatch != null) {
            logger.info("Found exact match for NAICS code: {}", searchTerm)
            return listOf(exactMatch)
        }

        // Then try codes starting with the search term
        val codeMatches = naicsCodeRepository.findByCodeStartingWith(searchTerm)
        if (codeMatches.isNotEmpty()) {
            logger.info("Found {} NAICS codes starting with: {}", codeMatches.size, searchTerm)
            return codeMatches
        }

        // Finally try general search in both code and title
        val results = naicsCodeRepository.searchByCodeOrTitle(searchTerm)
        logger.info("Found {} NAICS codes matching search term: {}", results.size, searchTerm)
        return results
    }
} 
