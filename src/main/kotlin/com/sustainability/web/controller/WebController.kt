package com.sustainability.web.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

/**
 * Controller for web pages (non-API endpoints)
 */
@Controller
class WebController {

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
     * Serve the welcome page for first-time users
     */
    @GetMapping("/welcome")
    fun welcome(): ModelAndView {
        return ModelAndView("welcome")
    }

    /**
     * Serve the onboarding page
     */
    @GetMapping("/onboarding")
    fun onboarding(): ModelAndView {
        return ModelAndView("onboarding")
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
    fun entityHierarchy(): ModelAndView {
        return ModelAndView("entity-hierarchy")
    }

    /**
     * Serve the data source setup page
     */
    @GetMapping("/data-source-setup")
    fun dataSourceSetup(): ModelAndView {
        return ModelAndView("data-source-setup")
    }
} 
