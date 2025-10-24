package com.sustainability.impacts.controller

import com.sustainability.impacts.model.NaicsCode
import com.sustainability.impacts.model.NaicsCodeDto
import com.sustainability.impacts.service.NaicsCodeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * DEPRECATED: This controller has been replaced by IndustryClassificationController
 * which provides a comprehensive API for all industry classification systems.
 * This class is kept for reference purposes only and should not be used in new code.
 */
// @RestController
// @RequestMapping("/api/v1/naics")
@Deprecated("Replaced by IndustryClassificationController", ReplaceWith("IndustryClassificationController"))
class LegacyNaicsCodeController @Autowired constructor(
    private val naicsCodeService: NaicsCodeService,
) {

    /**
     * Get all top-level NAICS codes
     */
    // @GetMapping
    fun getTopLevelCodes(): ResponseEntity<List<NaicsCode>> {
        return ResponseEntity.ok(naicsCodeService.getTopLevelCodes())
    }

    /**
     * Get child NAICS codes for a parent code
     */
    // @GetMapping("/children/{parentCode}")
    fun getChildCodes(@PathVariable parentCode: String): ResponseEntity<List<NaicsCode>> {
        return ResponseEntity.ok(naicsCodeService.getChildCodes(parentCode))
    }

    /**
     * Get a specific NAICS code by code value
     */
    // @GetMapping("/{code}")
    fun getByCode(@PathVariable code: String): ResponseEntity<NaicsCode> {
        val naicsCode = naicsCodeService.getByCode(code) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(naicsCode)
    }

    /**
     * Get the full hierarchy for a NAICS code
     */
    // @GetMapping("/{code}/hierarchy")
    fun getHierarchy(@PathVariable code: String): ResponseEntity<List<NaicsCode>> {
        val hierarchy = naicsCodeService.getHierarchy(code)
        if (hierarchy.isEmpty()) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(hierarchy)
    }

    /**
     * Get the full nested structure of NAICS codes
     */
    // @GetMapping("/nested")
    fun getNestedCodes(): ResponseEntity<List<NaicsCodeDto>> {
        return ResponseEntity.ok(naicsCodeService.buildNestedCodes())
    }

    /**
     * Search NAICS codes by title
     */
    // @GetMapping("/search")
    fun searchByTitle(@RequestParam("q") searchTerm: String): ResponseEntity<List<NaicsCode>> {
        return ResponseEntity.ok(naicsCodeService.searchByTitle(searchTerm))
    }
} 
