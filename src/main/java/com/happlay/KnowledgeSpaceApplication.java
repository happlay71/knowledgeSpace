package com.happlay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties
public class KnowledgeSpaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeSpaceApplication.class, args);
    }

}
