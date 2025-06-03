package org.example.appsmallcrm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Your Project API",
                version = "1.0",
                description = "API documentation for your project"
        )
)
public class SwaggerConfig {
}
