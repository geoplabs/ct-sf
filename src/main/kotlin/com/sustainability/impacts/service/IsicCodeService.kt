package com.sustainability.impacts.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.sustainability.impacts.model.IsicCode
import com.sustainability.impacts.model.IsicCodeDto
import com.sustainability.impacts.repository.IsicCodeRepository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class IsicCodeService @Autowired constructor(
    private val isicCodeRepository: IsicCodeRepository,
    private val objectMapper: ObjectMapper,
) {

    /**
     * Initialize ISIC codes from JSON file if the database is empty
     */
    @PostConstruct
    @Transactional
    fun initIsicCodes() {
        if (isicCodeRepository.count() == 0L) {
            val resource = ClassPathResource("static/data/isic_codes.json")
            val isicCodes: List<Map<String, Any>> = objectMapper.readValue(
                resource.inputStream,
                object : TypeReference<List<Map<String, Any>>>() {},
            )

            val allIsicCodes = mutableListOf<IsicCode>()

            // Process the nested structure of the ISIC codes JSON
            for (section in isicCodes) {
                val divisions = section["divisions"] as? List<Map<String, Any>> ?: continue

                for (division in divisions) {
                    val groups = division["groups"] as? List<Map<String, Any>> ?: continue

                    for (group in groups) {
                        val classes = group["classes"] as? List<Map<String, Any>> ?: continue

                        for (classMap in classes) {
                            val isicCode = IsicCode(
                                id = UUID.randomUUID().toString(),
                                code = classMap["class_code"].toString(),
                                title = classMap["title"].toString(),
                                section = section["section_code"]?.toString(),
                                division = division["division_code"]?.toString(),
                                group = group["group_code"]?.toString(),
                                revision = "4", // Default to Revision 4
                            )
                            allIsicCodes.add(isicCode)
                        }
                    }
                }
            }

            if (allIsicCodes.isNotEmpty()) {
                isicCodeRepository.saveAll(allIsicCodes)
            }
        }
    }

    /**
     * Get an ISIC code by its code value
     */
    fun getByCode(code: String): IsicCode? {
        return isicCodeRepository.findByCode(code)
    }

    /**
     * Search ISIC codes by title
     */
    fun searchByTitle(searchTerm: String): List<IsicCode> {
        return isicCodeRepository.searchByTitle(searchTerm)
    }

    /**
     * Get ISIC codes by section
     */
    fun getBySection(section: String): List<IsicCode> {
        return isicCodeRepository.findBySection(section)
    }

    /**
     * Get ISIC codes by division
     */
    fun getByDivision(division: String): List<IsicCode> {
        return isicCodeRepository.findByDivision(division)
    }

    /**
     * Get ISIC codes by group
     */
    fun getByGroup(group: String): List<IsicCode> {
        return isicCodeRepository.findByGroup(group)
    }

    /**
     * Convert IsicCode to IsicCodeDto
     */
    fun toDto(isicCode: IsicCode): IsicCodeDto {
        return IsicCodeDto(
            code = isicCode.code,
            title = isicCode.title,
            section = isicCode.section,
            division = isicCode.division,
            group = isicCode.group,
            revision = isicCode.revision,
        )
    }
} 
