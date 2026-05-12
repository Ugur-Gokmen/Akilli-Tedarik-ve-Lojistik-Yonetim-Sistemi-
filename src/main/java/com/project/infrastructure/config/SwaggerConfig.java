package com.project.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info; // Aradığın sınıf burada
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                .title("Akıllı Tedarik ve Lojistik API")
                .version("1.0")
                .description("OOAD Projesi için geliştirilen RESTful servis dokümantasyonu"));
    }
}
