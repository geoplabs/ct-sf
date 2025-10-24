package com.sustainability.web.viewmodel

import com.sustainability.web.onboarding.OnboardingProgress

data class IntegrationSetupForm(
    var accountId: String? = null,
    var erpSystem: String? = null,
    var dataAutomationPriority: String? = null,
    var integrationOwner: String? = null,
    var dataRefreshFrequency: String? = null,
) {
    companion object {
        fun fromData(accountId: String, data: OnboardingProgress.IntegrationData?): IntegrationSetupForm {
            return IntegrationSetupForm(
                accountId = accountId,
                erpSystem = data?.erpSystem,
                dataAutomationPriority = data?.dataAutomationPriority,
                integrationOwner = data?.integrationOwner,
                dataRefreshFrequency = data?.dataRefreshFrequency,
            )
        }
    }
}
