package com.extractor.unraveldocs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicAPI() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Unravel Docs API")
                        .version("1.0.0")
                        .description("API documentation for Unravel Docs"))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local server"),
                        new Server()
                                .url("https://unravel-docs.com")
                                .description("Production server")
                ))
                .components(new Components()
                        .addSchemas("profilePicture", new Schema<>()
                                .type("string")
                                .format("binary")
                                .description("File upload"))
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .security(Collections.singletonList(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                ));
    }
}
