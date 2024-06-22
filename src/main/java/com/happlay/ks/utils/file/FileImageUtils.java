package com.happlay.ks.utils.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileImageUtils {

    @Value("${file.storage.root.path}")
    private String storageRootPath;

    // 替换MD里的图片
    public String replacePathsInMD(byte[] fileBytes, Map<String, String> imagePathMap) {
        String content = new String(fileBytes, StandardCharsets.UTF_8);
        String basePath = new File(storageRootPath).getAbsolutePath();
        System.out.println(basePath);

        for (Map.Entry<String, String> entry : imagePathMap.entrySet()) {
            String originalPath = entry.getKey();
            String serverPath = entry.getValue();
            String path = Paths.get(basePath, serverPath).normalize().toString();
            content = content.replace(originalPath, path);
        }

        return content;
    }

    // 提取MD文件中的图片路径
    public List<String> extractImagePathsFromMD(byte[] fileBytes) {
        String content = new String(fileBytes, StandardCharsets.UTF_8);
        List<String> imagePaths = new ArrayList<>();
        Pattern pattern = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            imagePaths.add(matcher.group(1));
        }
        return imagePaths;
    }

    public byte[] readImage(String imagePath) throws IOException {
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            // 处理 URL 情况
            URL url = new URL(imagePath);
            try (InputStream in = url.openStream(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = in.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                return buffer.toByteArray();
            }
        } else {
            // 处理本地文件路径情况
            Path path = Paths.get(imagePath);
            return Files.readAllBytes(path);
        }
    }

}
