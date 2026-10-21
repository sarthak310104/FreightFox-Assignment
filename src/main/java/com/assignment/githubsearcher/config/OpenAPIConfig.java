package com.assignment.githubsearcher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI githubSearcherOpenApi() {
        return new OpenAPI().info(new Info()
                .title("GitHub Repository Searcher API")
                .version("1.0.0")
                .description("Searches GitHub repositories by name, language and sort order, "
                        + "stores the results in PostgreSQL (updating existing rows instead of "
                        + "duplicating them), and retrieves stored results with optional filters."));
    }
}