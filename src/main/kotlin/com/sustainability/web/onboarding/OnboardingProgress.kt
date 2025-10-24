package com.sustainability.web.onboarding

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("ONBOARDING_PROGRESS")
data class OnboardingProgress(
    @Id
    val id: String? = null,
    val accountId: String,
    var renewableEnergy: RenewableEnergyData? = null,
    var verification: VerificationData? = null,
    var integration: IntegrationData? = null,
    var finalReview: FinalReviewData? = null,
    var updatedAt: Instant = Instant.now(),
) {
    data class RenewableEnergyData(
        val annualDemandMwh: String? = null,
        val renewableTargetPercent: String? = null,
        val targetYear: String? = null,
        val procurementMechanisms: List<String>? = null,
        val storageStrategy: String? = null,
        val offsetsStrategy: String? = null,
    )

    data class VerificationData(
        val requiresThirdParty: Boolean = false,
        val lastAuditYear: String? = null,
        val qaOwner: String? = null,
        val materialityThreshold: String? = null,
        val uncertaintyApproach: String? = null,
    )

    data class IntegrationData(
        val erpSystem: String? = null,
        val dataAutomationPriority: String? = null,
        val integrationOwner: String? = null,
        val dataRefreshFrequency: String? = null,
    )

    data class FinalReviewData(
        val executiveSponsor: String? = null,
        val targetGoLive: String? = null,
        val outstandingRisks: String? = null,
        val nextSteps: String? = null,
    )
}
