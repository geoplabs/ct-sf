package com.sustainability.impacts.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.sustainability.impacts.model.SicCode
import com.sustainability.impacts.model.SicCodeDto
import com.sustainability.impacts.repository.SicCodeRepository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SicCodeService @Autowired constructor(
    private val sicCodeRepository: SicCodeRepository,
    private val objectMapper: ObjectMapper,
) {

    /**
     * Initialize SIC codes from JSON file if the database is empty
     */
    @PostConstruct
    @Transactional
    fun initSicCodes() {
        if (sicCodeRepository.count() == 0L) {
            val resource = ClassPathResource("static/data/sic_codes.json")
            val sicCodes: List<Map<String, Any>> = objectMapper.readValue(
                resource.inputStream,
                object : TypeReference<List<Map<String, Any>>>() {},
            )

            val allSicCodes = mutableListOf<SicCode>()

            // Process the nested structure of the SIC codes JSON
            for (division in sicCodes) {
                val majorGroups = division["major_groups"] as? List<Map<String, Any>> ?: continue

                for (majorGroup in majorGroups) {
                    val industryGroups = majorGroup["industry_groups"] as? List<Map<String, Any>> ?: continue

                    for (industryGroup in industryGroups) {
                        val sicCodesList = industryGroup["sic_codes"] as? List<Map<String, Any>> ?: continue

                        for (sicCodeMap in sicCodesList) {
                            val sicCode = SicCode(
                                id = UUID.randomUUID().toString(),
                                code = sicCodeMap["sic_code"].toString(),
                                title = sicCodeMap["description"].toString(),
                                division = division["division_code"]?.toString(),
                                majorGroup = majorGroup["major_group_code"]?.toString(),
                            )
                            allSicCodes.add(sicCode)
                        }
                    }
                }
            }

            if (allSicCodes.isNotEmpty()) {
                sicCodeRepository.saveAll(allSicCodes)
            }
        }
    }

    /**
     * Get a SIC code by its code value
     */
    fun getByCode(code: String): SicCode? {
        return sicCodeRepository.findByCode(code)
    }

    /**
     * Search SIC codes by title
     */
    fun searchByTitle(searchTerm: String): List<SicCode> {
        return sicCodeRepository.searchByTitle(searchTerm)
    }

    /**
     * Get SIC codes by division
     */
    fun getByDivision(division: String): List<SicCode> {
        return sicCodeRepository.findByDivision(division)
    }

    /**
     * Get SIC codes by major group
     */
    fun getByMajorGroup(majorGroup: String): List<SicCode> {
        return sicCodeRepository.findByMajorGroup(majorGroup)
    }

    /**
     * Convert SicCode to SicCodeDto
     */
    fun toDto(sicCode: SicCode): SicCodeDto {
        return SicCodeDto(
            code = sicCode.code,
            title = sicCode.title,
            division = sicCode.division,
            majorGroup = sicCode.majorGroup,
        )
    }
} 
