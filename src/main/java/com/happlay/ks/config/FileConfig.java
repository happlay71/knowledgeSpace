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
    private String filePath;   // 设置上传的笔记文件路径
    private String documentPath;  // 设置创建的笔记文件路径
}
