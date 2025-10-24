package com.sustainability.web.viewmodel

import com.sustainability.web.onboarding.OnboardingProgress

data class VerificationForm(
    var accountId: String? = null,
    var requiresThirdParty: Boolean = false,
    var lastAuditYear: String? = null,
    var qaOwner: String? = null,
    var materialityThreshold: String? = null,
    var uncertaintyApproach: String? = null,
) {
    companion object {
        fun fromData(accountId: String, data: OnboardingProgress.VerificationData?): VerificationForm {
            return VerificationForm(
                accountId = accountId,
                requiresThirdParty = data?.requiresThirdParty ?: false,
                lastAuditYear = data?.lastAuditYear,
                qaOwner = data?.qaOwner,
                materialityThreshold = data?.materialityThreshold,
                uncertaintyApproach = data?.uncertaintyApproach,
            )
        }
    }
}
