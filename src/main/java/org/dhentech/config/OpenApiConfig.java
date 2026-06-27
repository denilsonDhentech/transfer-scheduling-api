package org.dhentech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transferSchedulingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transfer Scheduling API")
                        .description("API para agendamento de transferências financeiras com cálculo automático de taxas.")
                        .version("1.0.0"));
    }
}
