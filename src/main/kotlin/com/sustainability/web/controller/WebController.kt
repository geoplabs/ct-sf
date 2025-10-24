package com.sustainability.web.controller
import com.sustainability.web.onboarding.OnboardingProgressService
import com.sustainability.web.viewmodel.FinalReviewForm
import com.sustainability.web.viewmodel.IntegrationSetupForm
import com.sustainability.web.viewmodel.RenewableEnergyForm
import com.sustainability.web.viewmodel.VerificationForm
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

/**
 * Controller for web pages (non-API endpoints)
 */
@Controller
class WebController(
    private val onboardingProgressService: OnboardingProgressService,
) {

    companion object {
        private const val DEFAULT_ACCOUNT_ID = "demo-account"
    }

    private fun resolveAccountId(accountId: String?): String {
        return accountId?.takeIf { it.isNotBlank() } ?: DEFAULT_ACCOUNT_ID
    }

    /**
     * Serve the main index page
     */
    @GetMapping("/")
    fun index(): ModelAndView {
        return ModelAndView("redirect:/login")
    }

    /**
     * Serve the signup page
     */
    @GetMapping("/signup")
    fun signup(): ModelAndView {
        return ModelAndView("signup")
    }

    /**
     * Serve the signup success page
     */
    @GetMapping("/signup-success")
    fun signupSuccess(): ModelAndView {
        return ModelAndView("signup-success")
    }

    /**
     * Serve the login page
     */
    @GetMapping("/login")
    fun login(): ModelAndView {
        return ModelAndView("login")
    }

     /**
     * Sign the user out and redirect to login
     */
    @GetMapping("/logout")
    fun logout(): ModelAndView {
        return ModelAndView("redirect:/login")
    }

    /**
     * Serve the welcome page for first-time users
     */
    @GetMapping("/welcome")
    fun welcome(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        return ModelAndView("welcome").apply {
            addObject("accountId", resolvedAccountId)
        }
    }

    /**
     * Serve the onboarding page
     */
    @GetMapping("/onboarding")
    fun onboarding(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        return ModelAndView("onboarding").apply {
            addObject("accountId", resolvedAccountId)
        }
    }

    /**
     * Serve the dashboard page
     */
    @GetMapping("/dashboard")
    fun dashboard(): ModelAndView {
        return ModelAndView("dashboard")
    }

    /**
     * Serve the entity hierarchy page
     */
    @GetMapping("/entity-hierarchy")
    fun entityHierarchy(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        return ModelAndView("entity-hierarchy").apply {
            addObject("accountId", resolvedAccountId)
        }
    }

    /**
     * Serve the data source setup page
     */
    @GetMapping("/data-source-setup")
    fun dataSourceSetup(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        return ModelAndView("data-source-setup").apply {
            addObject("accountId", resolvedAccountId)
        }
    }

    /**
     * Serve the renewable energy and offsets configuration page
     */
    @GetMapping("/renewable-energy")
    fun renewableEnergy(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        val progress = onboardingProgressService.getOrCreate(resolvedAccountId)
        return ModelAndView("renewable-energy").apply {
            addObject("accountId", resolvedAccountId)
            addObject("onboardingProgress", progress)
            addObject(
                "renewableEnergyForm",
                RenewableEnergyForm.fromData(resolvedAccountId, progress.renewableEnergy),
            )
        }
    }

    /**
     * Persist renewable energy configuration updates
     */
    @PostMapping("/renewable-energy")
    fun saveRenewableEnergy(
        @ModelAttribute("renewableEnergyForm") form: RenewableEnergyForm,
    ): ModelAndView {
        val resolvedAccountId = resolveAccountId(form.accountId)
        onboardingProgressService.updateRenewableEnergy(resolvedAccountId, form)
        return ModelAndView("redirect:/renewable-energy?accountId=$resolvedAccountId")
    }

    /**
     * Serve the verification and data quality page
     */
    @GetMapping("/verification")
    fun verification(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        val progress = onboardingProgressService.getOrCreate(resolvedAccountId)
        return ModelAndView("verification").apply {
            addObject("accountId", resolvedAccountId)
            addObject("onboardingProgress", progress)
            addObject(
                "verificationForm",
                VerificationForm.fromData(resolvedAccountId, progress.verification),
            )
        }
    }

    /**
     * Persist verification configuration updates
     */
    @PostMapping("/verification")
    fun saveVerification(
        @ModelAttribute("verificationForm") form: VerificationForm,
    ): ModelAndView {
        val resolvedAccountId = resolveAccountId(form.accountId)
        onboardingProgressService.updateVerification(resolvedAccountId, form)
        return ModelAndView("redirect:/verification?accountId=$resolvedAccountId")
    }

    /**
     * Serve the integration setup page
     */
    @GetMapping("/integration-setup")
    fun integrationSetup(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        val progress = onboardingProgressService.getOrCreate(resolvedAccountId)
        return ModelAndView("integration-setup").apply {
            addObject("accountId", resolvedAccountId)
            addObject("onboardingProgress", progress)
            addObject(
                "integrationSetupForm",
                IntegrationSetupForm.fromData(resolvedAccountId, progress.integration),
            )
        }
    }

    /**
     * Persist integration setup updates
     */
    @PostMapping("/integration-setup")
    fun saveIntegrationSetup(
        @ModelAttribute("integrationSetupForm") form: IntegrationSetupForm,
    ): ModelAndView {
        val resolvedAccountId = resolveAccountId(form.accountId)
        onboardingProgressService.updateIntegration(resolvedAccountId, form)
        return ModelAndView("redirect:/integration-setup?accountId=$resolvedAccountId")
    }

    /**
     * Serve the final review page
     */
    @GetMapping("/final-review")
    fun finalReview(@RequestParam(required = false) accountId: String?): ModelAndView {
        val resolvedAccountId = resolveAccountId(accountId)
        val progress = onboardingProgressService.getOrCreate(resolvedAccountId)
        return ModelAndView("final-review").apply {
            addObject("accountId", resolvedAccountId)
            addObject("onboardingProgress", progress)
            addObject(
                "finalReviewForm",
                FinalReviewForm.fromData(resolvedAccountId, progress.finalReview),
            )
        }
    }

    /**
     * Persist final review notes
     */
    @PostMapping("/final-review")
    fun saveFinalReview(
        @ModelAttribute("finalReviewForm") form: FinalReviewForm,
    ): ModelAndView {
        val resolvedAccountId = resolveAccountId(form.accountId)
        onboardingProgressService.updateFinalReview(resolvedAccountId, form)
        return ModelAndView("redirect:/final-review?accountId=$resolvedAccountId")
    }
}