package com.sustainability.impacts.service

import com.sustainability.impacts.repository.NaicsCodeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class NaicsCodeServiceIntegrationTest {

    @Autowired
    private lateinit var naicsCodeService: NaicsCodeService

    @Autowired
    private lateinit var naicsCodeRepository: NaicsCodeRepository

    @Test
    fun `initialization loads NAICS codes from JSON`() {
        // This test relies on the PostConstruct method having already run
        val count = naicsCodeRepository.count()
        assertTrue(count > 0, "NAICS codes should be loaded from JSON")
    }

    @Test
    fun `get top level codes returns all sector codes`() {
        val topLevelCodes = naicsCodeService.getTopLevelCodes()
        assertFalse(topLevelCodes.isEmpty(), "Should return top level NAICS codes")

        // All top level codes should have a level of 1
        topLevelCodes.forEach { code ->
            assertEquals(1, code.level, "Top level code should have level 1")
            assertNull(code.parentCode, "Top level code should have null parent code")
        }
    }

    @Test
    fun `get hierarchy returns full parent chain`() {
        // First get a leaf code
        val leafCodeOpt = naicsCodeRepository.findByLevel(5).firstOrNull()
        if (leafCodeOpt != null) {
            val hierarchy = naicsCodeService.getHierarchy(leafCodeOpt.code)

            // Hierarchy should have entries for all levels up to the leaf
            assertEquals(5, hierarchy.size, "Hierarchy should include all levels")

            // Check that hierarchy is in correct order (top level first)
            assertEquals(1, hierarchy[0].level, "First element should be top level")
            assertEquals(5, hierarchy[4].level, "Last element should be leaf level")

            // Verify parent-child relationships
            for (i in 1 until hierarchy.size) {
                assertEquals(
                    hierarchy[i - 1].code,
                    hierarchy[i].parentCode,
                    "Each code should reference its parent",
                )
            }
        }
    }
} 
