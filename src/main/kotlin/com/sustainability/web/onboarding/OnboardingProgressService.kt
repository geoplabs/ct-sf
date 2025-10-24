package com.sustainability.web.onboarding

import com.sustainability.web.viewmodel.FinalReviewForm
import com.sustainability.web.viewmodel.IntegrationSetupForm
import com.sustainability.web.viewmodel.RenewableEnergyForm
import com.sustainability.web.viewmodel.VerificationForm
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OnboardingProgressService(
    private val repository: OnboardingProgressRepository,
) {
    private fun normalize(input: String?): String? = input?.trim()?.takeIf { it.isNotEmpty() }

    fun getOrCreate(accountId: String): OnboardingProgress {
        return repository.findByAccountId(accountId) ?: repository.save(
            OnboardingProgress(accountId = accountId, updatedAt = Instant.now())
        )
    }

    fun updateRenewableEnergy(accountId: String, form: RenewableEnergyForm) {
        val progress = getOrCreate(accountId)
        progress.renewableEnergy = OnboardingProgress.RenewableEnergyData(
            annualDemandMwh = normalize(form.annualDemandMwh),
            renewableTargetPercent = normalize(form.renewableTargetPercent),
            targetYear = normalize(form.targetYear),
            procurementMechanisms = form.procurementMechanisms
                .mapNotNull { normalize(it) },
            storageStrategy = normalize(form.storageStrategy),
            offsetsStrategy = normalize(form.offsetsStrategy),
        )
        progress.updatedAt = Instant.now()
        repository.save(progress)
    }

    fun updateVerification(accountId: String, form: VerificationForm) {
        val progress = getOrCreate(accountId)
        progress.verification = OnboardingProgress.VerificationData(
            requiresThirdParty = form.requiresThirdParty,
            lastAuditYear = normalize(form.lastAuditYear),
            qaOwner = normalize(form.qaOwner),
            materialityThreshold = normalize(form.materialityThreshold),
            uncertaintyApproach = normalize(form.uncertaintyApproach),
        )
        progress.updatedAt = Instant.now()
        repository.save(progress)
    }

    fun updateIntegration(accountId: String, form: IntegrationSetupForm) {
        val progress = getOrCreate(accountId)
        progress.integration = OnboardingProgress.IntegrationData(
            erpSystem = normalize(form.erpSystem),
            dataAutomationPriority = normalize(form.dataAutomationPriority),
            integrationOwner = normalize(form.integrationOwner),
            dataRefreshFrequency = normalize(form.dataRefreshFrequency),
        )
        progress.updatedAt = Instant.now()
        repository.save(progress)
    }

    fun updateFinalReview(accountId: String, form: FinalReviewForm) {
        val progress = getOrCreate(accountId)
        progress.finalReview = OnboardingProgress.FinalReviewData(
            executiveSponsor = normalize(form.executiveSponsor),
            targetGoLive = normalize(form.targetGoLive),
            outstandingRisks = normalize(form.outstandingRisks),
            nextSteps = normalize(form.nextSteps),
        )
        progress.updatedAt = Instant.now()
        repository.save(progress)
    }
}
