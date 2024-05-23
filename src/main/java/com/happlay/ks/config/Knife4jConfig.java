package com.happlay.ks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;


@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2) // 使用 OAS 3.0 文档类型
                .apiInfo(new ApiInfoBuilder()
                        .title("接口文档")
                        .description("KnowledgeSpace")
                        .version("1.0")
                        .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0.html")
                        .contact(new Contact("happlay", null, "1633433173@qq.com"))
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.happlay.ks.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
