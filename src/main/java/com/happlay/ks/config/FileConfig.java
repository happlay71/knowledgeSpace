package com.happlay.ks.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileConfig {
    private long maxMb;
    private String avatarPath; // 设置头像路径
    private String filePath;   // 设置笔记文件路径
}
