package com.sustainability.impacts.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.sustainability.impacts.model.CrosswalkSuggestionDto
import com.sustainability.impacts.model.IndustryCrosswalk
import com.sustainability.impacts.repository.IndustryCrosswalkRepository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class IndustryCrosswalkService @Autowired constructor(
    private val crosswalkRepository: IndustryCrosswalkRepository,
    private val naicsCodeService: NaicsCodeService,
    private val sicCodeService: SicCodeService,
    private val isicCodeService: IsicCodeService,
    private val objectMapper: ObjectMapper,
) {

    /**
     * Initialize crosswalk mappings from JSON file if the database is empty
     */
    @PostConstruct
    @Transactional
    fun initCrosswalkMappings() {
        if (crosswalkRepository.count() == 0L) {
            val resource = ClassPathResource("static/data/industry_crosswalk.json")
            val crosswalkMappings: List<Map<String, Any>> = objectMapper.readValue(
                resource.inputStream,
                object : TypeReference<List<Map<String, Any>>>() {},
            )

            val crosswalkEntities = crosswalkMappings.map { mapping ->
                IndustryCrosswalk(
                    id = UUID.randomUUID().toString(),
                    naicsCode = mapping["naicsCode"].toString(),
                    sicCode = mapping["sicCode"]?.toString(),
                    isicCode = mapping["isicCode"]?.toString(),
                    naicsYear = mapping["naicsYear"]?.toString() ?: "2022",
                    sicYear = mapping["sicYear"]?.toString() ?: "1987",
                    isicRevision = mapping["isicRevision"]?.toString() ?: "4",
                )
            }

            crosswalkRepository.saveAll(crosswalkEntities)
        }
    }

    /**
     * Get suggested codes based on a NAICS code
     */
    fun getSuggestionsByNaics(naicsCode: String): CrosswalkSuggestionDto {
        val naicsCodeEntity = naicsCodeService.getByCode(naicsCode)
        val crosswalks = crosswalkRepository.findByNaicsCode(naicsCode)

        // If no mappings exist, return just the NAICS info
        if (crosswalks.isEmpty()) {
            return CrosswalkSuggestionDto(
                naicsCode = naicsCode,
                naicsTitle = naicsCodeEntity?.title,
            )
        }

        // Get the first mapping (assume it's the most relevant)
        val firstMapping = crosswalks[0]

        // Look up additional info for SIC and ISIC codes
        val sicCode = firstMapping.sicCode?.let { sicCodeService.getByCode(it) }
        val isicCode = firstMapping.isicCode?.let { isicCodeService.getByCode(it) }

        return CrosswalkSuggestionDto(
            naicsCode = naicsCode,
            naicsTitle = naicsCodeEntity?.title,
            sicCode = firstMapping.sicCode,
            sicTitle = sicCode?.title,
            isicCode = firstMapping.isicCode,
            isicTitle = isicCode?.title,
        )
    }

    /**
     * Get suggested codes based on a SIC code
     */
    fun getSuggestionsBySic(sicCode: String): CrosswalkSuggestionDto {
        val sicCodeEntity = sicCodeService.getByCode(sicCode)
        val crosswalks = crosswalkRepository.findBySicCode(sicCode)

        // If no mappings exist, return just the SIC info
        if (crosswalks.isEmpty()) {
            return CrosswalkSuggestionDto(
                sicCode = sicCode,
                sicTitle = sicCodeEntity?.title,
            )
        }

        // Get the first mapping (assume it's the most relevant)
        val firstMapping = crosswalks[0]

        // Look up additional info for NAICS and ISIC codes
        val naicsCode = firstMapping.naicsCode.let { naicsCodeService.getByCode(it) }
        val isicCode = firstMapping.isicCode?.let { isicCodeService.getByCode(it) }

        return CrosswalkSuggestionDto(
            naicsCode = firstMapping.naicsCode,
            naicsTitle = naicsCode?.title,
            sicCode = sicCode,
            sicTitle = sicCodeEntity?.title,
            isicCode = firstMapping.isicCode,
            isicTitle = isicCode?.title,
        )
    }

    /**
     * Get suggested codes based on an ISIC code
     */
    fun getSuggestionsByIsic(isicCode: String): CrosswalkSuggestionDto {
        val isicCodeEntity = isicCodeService.getByCode(isicCode)
        val crosswalks = crosswalkRepository.findByIsicCode(isicCode)

        // If no mappings exist, return just the ISIC info
        if (crosswalks.isEmpty()) {
            return CrosswalkSuggestionDto(
                isicCode = isicCode,
                isicTitle = isicCodeEntity?.title,
            )
        }

        // Get the first mapping (assume it's the most relevant)
        val firstMapping = crosswalks[0]

        // Look up additional info for NAICS and SIC codes
        val naicsCode = firstMapping.naicsCode.let { naicsCodeService.getByCode(it) }
        val sicCode = firstMapping.sicCode?.let { sicCodeService.getByCode(it) }

        return CrosswalkSuggestionDto(
            naicsCode = firstMapping.naicsCode,
            naicsTitle = naicsCode?.title,
            sicCode = firstMapping.sicCode,
            sicTitle = sicCode?.title,
            isicCode = isicCode,
            isicTitle = isicCodeEntity?.title,
        )
    }
} 
