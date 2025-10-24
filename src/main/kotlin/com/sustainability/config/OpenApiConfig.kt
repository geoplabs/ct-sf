package com.sustainability.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for OpenAPI documentation
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Carbon Tracker API")
                    .version("1.0")
                    .description("API for tracking and managing carbon emissions data")
                    .contact(
                        Contact()
                            .name("Sustainability Team")
                            .email("sustainability@example.com"),
                    )
                    .license(
                        License()
                            .name("MIT")
                            .url("https://opensource.org/licenses/MIT"),
                    ),
            )
            .addServersItem(
                Server().url("/").description("Default Server URL"),
            )
    }
} 
