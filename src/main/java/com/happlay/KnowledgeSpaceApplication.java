package com.happlay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling  // 定时清理idDelete为1的用户
public class KnowledgeSpaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeSpaceApplication.class, args);
    }

}
