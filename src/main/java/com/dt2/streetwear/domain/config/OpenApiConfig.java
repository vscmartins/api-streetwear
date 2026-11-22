package com.dt2.streetwear.domain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Streetwear API")
                .description("API REST para gestao de produtos, estoque por tamanho/cor e controle transacional de vendas para loja de streetwear.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Victor Martins")
                    .email("cyberupstudio@gmail.com")));
    }
}
