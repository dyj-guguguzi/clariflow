package com.clariflow.workflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI clariFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClariFlow API")
                        .description("AI 辅助研发工作项流转与需求澄清系统 —— REST API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ClariFlow Team")
                                .email("team@clariflow.com")));
    }
}
