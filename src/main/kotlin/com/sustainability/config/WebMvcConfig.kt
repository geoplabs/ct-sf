package com.sustainability.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC configuration for the application
 * Handles cross-origin requests and API versioning
 */
@Configuration
class WebMvcConfig : WebMvcConfigurer {

    /**
     * Configure CORS for the API
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        // Apply CORS configuration to all API endpoints
        registry.addMapping("${ApiVersionConfig.API_V1_BASE}/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600)
    }
} 
