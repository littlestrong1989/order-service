package com.demo.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI()
            .info(new Info().title("Order Service API").version("1.0.0")
                .description("订单管理系统：RBAC 权限 + 乐观锁防超卖 + 库存扣减 + 分页检索"))
            .addSecurityItem(new SecurityRequirement().addList(SCHEME))
            .components(new Components().addSecuritySchemes(SCHEME,
                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                    .scheme("bearer").bearerFormat("JWT")));
    }
}
