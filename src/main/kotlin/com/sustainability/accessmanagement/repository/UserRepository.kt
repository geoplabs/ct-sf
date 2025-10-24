package com.sustainability.accessmanagement.repository

import com.sustainability.accessmanagement.model.Tags
import com.sustainability.accessmanagement.model.User
import com.sustainability.accessmanagement.model.UserState
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class UserRepository(private val mongoTemplate: MongoTemplate) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val collectionName = "USERS"

    /**
     * Create a new user
     */
    fun create(user: User): User {
        logger.debug("UserRepository > create > user: {}", user)
        try {
            logger.info(
                "Saving user to database: '{}', collection: '{}'",
                mongoTemplate.db.name,
                collectionName,
            )
            val result = mongoTemplate.insert(user, collectionName)
            logger.info("User saved successfully with id: {}", result.email)
            return result
        } catch (ex: Exception) {
            logger.error("Error creating user: {}", ex.message, ex)
            throw ex
        }
    }

    /**
     * Get a user by email
     */
    fun get(email: String): User? {
        logger.debug("UserRepository > get > email: {}", email)
        try {
            logger.info(
                "Getting user from database: '{}', collection: '{}'",
                mongoTemplate.db.name,
                collectionName,
            )
            val query = Query.query(Criteria.where("email").`is`(email))
            val result = mongoTemplate.findOne(query, User::class.java, collectionName)
            logger.info("User lookup result: {}", if (result != null) "FOUND" else "NOT FOUND")
            return result
        } catch (ex: Exception) {
            logger.error("Error getting user: {}", ex.message, ex)
            throw ex
        }
    }

    /**
     * Update a user
     */
    fun update(user: User, tagsToAdd: Tags?, tagsToDelete: Tags?): User {
        logger.debug(
            "UserRepository > update > user: {}, tagsToAdd: {}, tagsToDelete: {}",
            user,
            tagsToAdd,
            tagsToDelete,
        )

        val query = Query.query(Criteria.where("email").`is`(user.email))
        val update = Update()
            .set("firstName", user.firstName)
            .set("lastName", user.lastName)
            .set("state", user.state)
            .set("groups", user.groups)
            .set("defaultGroup", user.defaultGroup)
            .set("updatedAt", user.updatedAt ?: Instant.now())
            .set("updatedBy", user.updatedBy)

        // Handle tags updates
        val existingUser = get(user.email) ?: throw IllegalStateException("User not found")
        val updatedTags = existingUser.tags?.toMutableMap() ?: mutableMapOf()

        // Remove tags that should be deleted
        tagsToDelete?.keys?.forEach { updatedTags.remove(it) }

        // Add new tags
        tagsToAdd?.forEach { (key, value) -> updatedTags[key] = value }

        update.set("tags", updatedTags)

        mongoTemplate.updateFirst(query, update, User::class.java, collectionName)
        return get(user.email) ?: throw IllegalStateException("Failed to retrieve updated user")
    }

    /**
     * Delete a user
     */
    fun delete(email: String) {
        logger.debug("UserRepository > delete > email: {}", email)
        mongoTemplate.remove(
            Query.query(Criteria.where("email").`is`(email)),
            User::class.java,
            collectionName,
        )
    }

    /**
     * List users by IDs
     */
    fun listByIds(userIds: List<String>): List<User> {
        logger.debug("UserRepository > listByIds > userIds: {}", userIds)
        if (userIds.isEmpty()) return emptyList()

        return mongoTemplate.find(
            Query.query(Criteria.where("email").`in`(userIds)),
            User::class.java,
            collectionName,
        )
    }

    /**
     * List users with pagination and optional filtering
     */
    fun list(
        limit: Int? = null,
        fromEmail: String? = null,
        tags: Tags? = null,
        states: List<UserState>? = null,
    ): List<User> {
        logger.debug(
            "UserRepository > list > limit: {}, fromEmail: {}, tags: {}, states: {}",
            limit,
            fromEmail,
            tags,
            states,
        )

        val query = Query()

        // Apply email pagination (start after this email)
        fromEmail?.let {
            query.addCriteria(Criteria.where("email").gt(it))
        }

        // Apply tags filter if specified
        tags?.forEach { (key, value) ->
            query.addCriteria(Criteria.where("tags.$key").`is`(value))
        }

        // Apply state filter if specified
        states?.let {
            if (it.isNotEmpty()) {
                query.addCriteria(Criteria.where("state").`in`(it))
            }
        }

        // Apply limit if specified
        limit?.let { query.limit(it) }

        // Sort by email for consistent pagination
        query.with(org.springframework.data.domain.Sort.by("email"))

        return mongoTemplate.find(query, User::class.java, collectionName)
    }
} 
