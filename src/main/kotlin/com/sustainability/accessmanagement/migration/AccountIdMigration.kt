package com.sustainability.accessmanagement.migration

import com.sustainability.accessmanagement.repository.GroupRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

/**
 * Migration to move accountId from tags to the main field in Group model.
 * This migration should be run once during application startup after deploying the new version.
 */
@Component
class AccountIdMigration(
    private val mongoTemplate: MongoTemplate,
    private val groupRepository: GroupRepository,
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val collectionName = "GROUPS"

    override fun run(vararg args: String?) {
        logger.info("Starting accountId migration from tags to main field")

        try {
            // Find all groups with accountId in tags
            val query = Query.query(Criteria.where("tags.accountId").exists(true))
            val groups = mongoTemplate.find(query, Map::class.java, collectionName)

            logger.info("Found ${groups.size} groups with accountId in tags")

            groups.forEach { group ->
                try {
                    // Safely extract values with proper null handling
                    val groupId = group["id"] as? String
                    if (groupId == null) {
                        logger.warn("Skipping group with null ID")
                        return@forEach
                    }

                    val tags = group["tags"] as? Map<*, *>
                    if (tags == null) {
                        logger.warn("Skipping group $groupId - tags field is null")
                        return@forEach
                    }

                    val accountId = tags["accountId"] as? String
                    if (accountId == null) {
                        logger.warn("Skipping group $groupId - accountId in tags is null")
                        return@forEach
                    }

                    logger.info("Migrating accountId for group: $groupId")

                    // Update the group to move accountId to main field
                    val updateQuery = Query.query(Criteria.where("id").`is`(groupId))
                    val update = Update()
                        .set("accountId", accountId)

                    mongoTemplate.updateFirst(updateQuery, update, collectionName)

                    // Remove accountId from tags
                    val tagsUpdate = Update().unset("tags.accountId")
                    mongoTemplate.updateFirst(updateQuery, tagsUpdate, collectionName)

                    logger.info("Successfully migrated accountId for group: $groupId")
                } catch (e: Exception) {
                    logger.error("Error processing group ${group["id"]}: ${e.message}", e)
                }
            }

            logger.info("AccountId migration completed successfully")
        } catch (e: Exception) {
            logger.error("Error during accountId migration: ${e.message}", e)
        }
    }
} 
