package com.sustainability.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Configuration for Jackson JSON processing
 */
@Configuration
class JacksonConfig {

    /**
     * Configures the ObjectMapper with settings for handling complex JSON structures
     * This configuration ensures maximum flexibility for handling dynamic tag structures
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule.Builder().build())
            .registerModule(SimpleModule())
            // Allow unknown properties in JSON to support extensible tag structures
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // Use the ISO-8601 date format instead of timestamps
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            // For handling deeply nested structures
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            // Allow empty JSON values like {} to be deserialized as nulls where appropriate
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            // Increase flexibility for numeric types
            .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
            .configure(DeserializationFeature.USE_LONG_FOR_INTS, true)

        return mapper
    }
} 
