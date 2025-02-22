package com.example.f_space.controller.swaggerconfig;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medication Compliance API")
                        .description("API for managing medication compliance and analytics")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")
                                .url("https://yourwebsite.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Additional Documentation")
                        .url("https://yourwebsite.com/docs"));
    }
}