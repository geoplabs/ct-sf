package com.sustainability.web.onboarding

import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class OnboardingProgressRepository(
    private val mongoTemplate: MongoTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val collectionName = "ONBOARDING_PROGRESS"

    fun findByAccountId(accountId: String): OnboardingProgress? {
        val query = Query.query(Criteria.where("accountId").`is`(accountId))
        return mongoTemplate.findOne(query, OnboardingProgress::class.java, collectionName)
    }

    fun save(progress: OnboardingProgress): OnboardingProgress {
        logger.info("Persisting onboarding progress for account {}", progress.accountId)
        return mongoTemplate.save(progress, collectionName)
    }
}
