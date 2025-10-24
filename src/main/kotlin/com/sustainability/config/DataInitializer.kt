package com.sustainability.config

import com.sustainability.accessmanagement.model.Group
import com.sustainability.accessmanagement.model.GroupState
import com.sustainability.accessmanagement.repository.GroupRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Initializes default data when the application starts
 */
@Component
class DataInitializer(
    private val groupRepository: GroupRepository
) : CommandLineRunner {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun run(vararg args: String?) {
        logger.info("Initializing default data...")
        
        try {
            // Create default groups if they don't exist
            createDefaultGroups()
            
            logger.info("Default data initialization completed successfully")
        } catch (e: Exception) {
            logger.error("Error during data initialization: ${e.message}", e)
        }
    }
    
    private fun createDefaultGroups() {
        val defaultGroups = listOf(
            "/sf" to "Svante Fourier",
            "/svantefourier" to "Svante Fourier Corporation"
        )
        
        defaultGroups.forEach { (groupId, description) ->
            try {
                val existingGroup = groupRepository.get(groupId)
                if (existingGroup == null) {
                    logger.info("Creating default group: $groupId")
                    
                    val defaultGroup = Group(
                        id = groupId,
                        description = description,
                        state = GroupState.ACTIVE,
                        tags = mapOf(
                            "naicsCode" to "541213",
                            "naicsDescription" to "Tax Preparation Services",
                            "businessDescription" to "Corporate sustainability services",
                            "headquartersAddress" to "Raheja mindspace",
                            "baselineYear" to "2022",
                            "reportingYearType" to "calendar",
                            "unitSystem" to "metric",
                            "gwpHorizon" to "AR4_100yr",
                            "netZeroCommitted" to "true",
                            "targetType" to "absolute",
                            "targetYear" to "2030",
                            "targetPercentage" to "30",
                            "isParentCompany" to "true",
                            "sustainabilityLeadName" to "Admin",
                            "sustainabilityLeadEmail" to "admin@svantefourier.com",
                            "financeLeadName" to "Finance Lead",
                            "financeLeadEmail" to "finance@svantefourier.com",
                            "dataResidency" to "eu-central-1",
                            "dpaAcceptance" to "true",
                            "retentionPeriod" to "7",
                            "onboardingCompleted" to "true"
                        ),
                        isParentCompany = true,
                        accountId = null,
                        configuration = null,
                        createdAt = Instant.now(),
                        createdBy = "system",
                        updatedAt = Instant.now(),
                        updatedBy = "system"
                    )
                    
                    groupRepository.create(defaultGroup)
                    logger.info("Successfully created default group: $groupId")
                } else {
                    logger.debug("Group $groupId already exists, skipping creation")
                }
            } catch (e: Exception) {
                logger.warn("Failed to create default group $groupId: ${e.message}")
            }
        }
    }
}
