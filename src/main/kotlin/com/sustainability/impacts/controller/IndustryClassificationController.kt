package com.sustainability.impacts.controller

import com.sustainability.config.ApiVersionConfig
import com.sustainability.impacts.model.CrosswalkSuggestionDto
import com.sustainability.impacts.model.IsicCodeDto
import com.sustainability.impacts.model.NaicsCodeDto
import com.sustainability.impacts.model.SicCodeDto
import com.sustainability.impacts.service.IndustryCrosswalkService
import com.sustainability.impacts.service.IsicCodeService
import com.sustainability.impacts.service.NaicsCodeService
import com.sustainability.impacts.service.SicCodeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiVersionConfig.API_V1_BASE)
class IndustryClassificationController @Autowired constructor(
    private val naicsCodeService: NaicsCodeService,
    private val sicCodeService: SicCodeService,
    private val isicCodeService: IsicCodeService,
    private val crosswalkService: IndustryCrosswalkService,
) {

    // === NAICS Code Endpoints ===

    @GetMapping("/naics")
    fun getTopLevelNaicsCodes(): ResponseEntity<List<NaicsCodeDto>> {
        val topLevelCodes = naicsCodeService.getTopLevelCodes()
            .map { NaicsCodeDto(code = it.code, title = it.title) }
        return ResponseEntity.ok(topLevelCodes)
    }

    @GetMapping("/naics/children/{parentCode}")
    fun getNaicsChildren(@PathVariable parentCode: String): ResponseEntity<List<NaicsCodeDto>> {
        val childCodes = naicsCodeService.getChildCodes(parentCode)
            .map { NaicsCodeDto(code = it.code, title = it.title) }
        return ResponseEntity.ok(childCodes)
    }

    @GetMapping("/naics/{code}")
    fun getNaicsCode(@PathVariable code: String): ResponseEntity<NaicsCodeDto> {
        val naicsCode = naicsCodeService.getByCode(code)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(NaicsCodeDto(code = naicsCode.code, title = naicsCode.title))
    }

    @GetMapping("/naics/{code}/hierarchy")
    fun getNaicsHierarchy(@PathVariable code: String): ResponseEntity<List<NaicsCodeDto>> {
        val hierarchy = naicsCodeService.getHierarchy(code)
            .map { NaicsCodeDto(code = it.code, title = it.title) }

        return ResponseEntity.ok(hierarchy)
    }

    @GetMapping("/naics/search")
    fun searchNaicsCodes(@RequestParam query: String): ResponseEntity<List<NaicsCodeDto>> {
        val results = naicsCodeService.searchByCodeOrTitle(query)
            .map { NaicsCodeDto(code = it.code, title = it.title) }

        return ResponseEntity.ok(results)
    }

    // === SIC Code Endpoints ===

    @GetMapping("/sic/{code}")
    fun getSicCode(@PathVariable code: String): ResponseEntity<SicCodeDto> {
        val sicCode = sicCodeService.getByCode(code)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(sicCodeService.toDto(sicCode))
    }

    @GetMapping("/sic/search")
    fun searchSicCodes(@RequestParam query: String): ResponseEntity<List<SicCodeDto>> {
        val results = sicCodeService.searchByTitle(query)
            .map { sicCodeService.toDto(it) }

        return ResponseEntity.ok(results)
    }

    // === ISIC Code Endpoints ===

    @GetMapping("/isic/{code}")
    fun getIsicCode(@PathVariable code: String): ResponseEntity<IsicCodeDto> {
        val isicCode = isicCodeService.getByCode(code)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(isicCodeService.toDto(isicCode))
    }

    @GetMapping("/isic/search")
    fun searchIsicCodes(@RequestParam query: String): ResponseEntity<List<IsicCodeDto>> {
        val results = isicCodeService.searchByTitle(query)
            .map { isicCodeService.toDto(it) }

        return ResponseEntity.ok(results)
    }

    // === Crosswalk Endpoints ===

    @GetMapping("/crosswalk/naics/{code}")
    fun getCrosswalkByNaics(@PathVariable code: String): ResponseEntity<CrosswalkSuggestionDto> {
        val suggestions = crosswalkService.getSuggestionsByNaics(code)
        return ResponseEntity.ok(suggestions)
    }

    @GetMapping("/crosswalk/sic/{code}")
    fun getCrosswalkBySic(@PathVariable code: String): ResponseEntity<CrosswalkSuggestionDto> {
        val suggestions = crosswalkService.getSuggestionsBySic(code)
        return ResponseEntity.ok(suggestions)
    }

    @GetMapping("/crosswalk/isic/{code}")
    fun getCrosswalkByIsic(@PathVariable code: String): ResponseEntity<CrosswalkSuggestionDto> {
        val suggestions = crosswalkService.getSuggestionsByIsic(code)
        return ResponseEntity.ok(suggestions)
    }
} 
