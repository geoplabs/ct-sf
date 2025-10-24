package com.sustainability.web.viewmodel

import com.sustainability.web.onboarding.OnboardingProgress

data class RenewableEnergyForm(
    var accountId: String? = null,
    var annualDemandMwh: String? = null,
    var renewableTargetPercent: String? = null,
    var targetYear: String? = null,
    var procurementMechanisms: MutableList<String> = mutableListOf(),
    var storageStrategy: String? = null,
    var offsetsStrategy: String? = null,
) {
    companion object {
        fun fromData(accountId: String, data: OnboardingProgress.RenewableEnergyData?): RenewableEnergyForm {
            return RenewableEnergyForm(
                accountId = accountId,
                annualDemandMwh = data?.annualDemandMwh,
                renewableTargetPercent = data?.renewableTargetPercent,
                targetYear = data?.targetYear,
                procurementMechanisms = data?.procurementMechanisms?.toMutableList() ?: mutableListOf(),
                storageStrategy = data?.storageStrategy,
                offsetsStrategy = data?.offsetsStrategy,
            )
        }
    }
}
