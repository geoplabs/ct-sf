package com.sustainability.accessmanagement.repository

import com.sustainability.accessmanagement.model.Group
import com.sustainability.accessmanagement.model.GroupState
import com.sustainability.accessmanagement.model.Tags
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class GroupRepository(private val mongoTemplate: MongoTemplate) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val collectionName = "GROUPS"

    /**
     * Create a new group
     */
    fun create(group: Group): Group {
        logger.debug("GroupRepository > create > group: {}", group)
        return mongoTemplate.insert(group, collectionName)
    }

    /**
     * Get a group by ID
     */
    fun get(id: String): Group? {
        logger.debug("GroupRepository > get > id: {}", id)
        return mongoTemplate.findOne(
            Query.query(Criteria.where("id").`is`(id)),
            Group::class.java,
            collectionName,
        )
    }

    /**
     * Update a group
     */
    fun update(group: Group, tagsToAdd: Tags?, tagsToDelete: Tags?): Group {
        logger.debug(
            "GroupRepository > update > group: {}, tagsToAdd: {}, tagsToDelete: {}",
            group,
            tagsToAdd,
            tagsToDelete,
        )

        val query = Query.query(Criteria.where("id").`is`(group.id))
        val update = Update()
            .set("name", group.name)
            .set("description", group.description)
            .set("state", group.state)
            .set("isParentCompany", group.isParentCompany)
            .set("accountId", group.accountId)
            .set("configuration", group.configuration)
            .set("updatedAt", group.updatedAt ?: Instant.now())
            .set("updatedBy", group.updatedBy)

        // Handle tags updates
        val existingGroup = get(group.id) ?: throw IllegalStateException("Group not found")
        val updatedTags = existingGroup.tags?.toMutableMap() ?: mutableMapOf()

        // Remove tags that should be deleted
        tagsToDelete?.keys?.forEach { updatedTags.remove(it) }

        // Add new tags
        tagsToAdd?.forEach { (key, value) -> updatedTags[key] = value }

        update.set("tags", updatedTags)

        mongoTemplate.updateFirst(query, update, Group::class.java, collectionName)
        return get(group.id) ?: throw IllegalStateException("Failed to retrieve updated group")
    }

    /**
     * Delete a group
     */
    fun delete(id: String) {
        logger.debug("GroupRepository > delete > id: {}", id)
        mongoTemplate.remove(
            Query.query(Criteria.where("id").`is`(id)),
            Group::class.java,
            collectionName,
        )
    }

    /**
     * List groups by IDs
     */
    fun listByIds(groupIds: List<String>): List<Group> {
        logger.debug("GroupRepository > listByIds > groupIds: {}", groupIds)
        if (groupIds.isEmpty()) return emptyList()

        return mongoTemplate.find(
            Query.query(Criteria.where("id").`in`(groupIds)),
            Group::class.java,
            collectionName,
        )
    }

    /**
     * Check if a group exists
     */
    fun exists(id: String): Boolean {
        logger.debug("GroupRepository > exists > id: {}", id)
        return mongoTemplate.exists(
            Query.query(Criteria.where("id").`is`(id)),
            Group::class.java,
            collectionName,
        )
    }

    /**
     * List groups with pagination and optional filtering
     */
    fun list(
        limit: Int? = null,
        fromGroupId: String? = null,
        tags: Tags? = null,
        accountId: String? = null,
        states: List<GroupState>? = null,
        parentGroupId: String? = null,
    ): List<Group> {
        logger.debug(
            "GroupRepository > list > limit: {}, fromGroupId: {}, tags: {}," +
                " accountId: {}, states: {}, parentGroupId: {}",
            limit,
            fromGroupId,
            tags,
            accountId,
            states,
            parentGroupId,
        )

        val query = Query()

        // Apply group ID pagination (start after this ID)
        fromGroupId?.let {
            query.addCriteria(Criteria.where("id").gt(it))
        }

        // Apply tags filter if specified
        tags?.forEach { (key, value) ->
            query.addCriteria(Criteria.where("tags.$key").`is`(value))
        }

        // Apply accountId filter if specified
        accountId?.let {
            query.addCriteria(Criteria.where("accountId").`is`(it))
        }

        // Apply state filter if specified
        states?.let {
            if (it.isNotEmpty()) {
                query.addCriteria(Criteria.where("state").`in`(it))
            }
        }

        // Apply parent group filter if specified
        parentGroupId?.let {
            if (it == "/") {
                // For root group, we want to include both the root and its direct children
                query.addCriteria(
                    Criteria().orOperator(
                        Criteria.where("id").`is`("/"),
                        Criteria.where("id").regex("^/[^/]+$"),
                    ),
                )
            } else {
                // For non-root groups, use the existing pattern
                query.addCriteria(
                    Criteria.where("id").regex("^$it/[^/]+$"),
                )
            }
        }

        // Apply limit if specified
        limit?.let { query.limit(it) }

        // Sort by ID for consistent pagination
        query.with(org.springframework.data.domain.Sort.by("id"))

        return mongoTemplate.find(query, Group::class.java, collectionName)
    }
} 
