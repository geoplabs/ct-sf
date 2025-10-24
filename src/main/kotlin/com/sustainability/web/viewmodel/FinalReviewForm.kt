package com.sustainability.web.viewmodel

import com.sustainability.web.onboarding.OnboardingProgress

data class FinalReviewForm(
    var accountId: String? = null,
    var executiveSponsor: String? = null,
    var targetGoLive: String? = null,
    var outstandingRisks: String? = null,
    var nextSteps: String? = null,
) {
    companion object {
        fun fromData(accountId: String, data: OnboardingProgress.FinalReviewData?): FinalReviewForm {
            return FinalReviewForm(
                accountId = accountId,
                executiveSponsor = data?.executiveSponsor,
                targetGoLive = data?.targetGoLive,
                outstandingRisks = data?.outstandingRisks,
                nextSteps = data?.nextSteps,
            )
        }
    }
}
