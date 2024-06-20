package com.happlay.ks.utils;

import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.config.FileConfig;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUtils {

    @Autowired
    private FileConfig fileConfig;

    private final ResourceLoader resourceLoader;

    @Value("${file.storage.root.path}")
    private String storageRootPath;

    @Autowired
    public FileUtils(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String storeTemp(byte[] data, FileTypeEnum fileTypeEnum, int id) {
        if (data == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "不存在临时文件");
        }

        String folderPath = getFolderPath(fileTypeEnum, id);
        createDirectoryIfNotExists(folderPath);

        String fileName = UUID.randomUUID().toString();
        File file = new File(folderPath, fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件失败");
        }
        return getRelativePath(fileTypeEnum, id, fileName);
    }

    public String saveFile(MultipartFile file, FileTypeEnum fileTypeEnum, int id) {
        if (file.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名为空");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String folderPath = getFolderPath(fileTypeEnum, id);
        createDirectoryIfNotExists(folderPath);

        String fileName = UUID.randomUUID().toString() + fileExtension;
        File destFile = new File(folderPath, fileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "保存文件失败");
        }
        return getRelativePath(fileTypeEnum, id, fileName);
    }

    public String createFile(String fileName, String content, FileTypeEnum fileTypeEnum, int id) {
        if (fileName == null || fileName.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名为空");
        }
        if (content == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件内容为空");
        }

        String folderPath = getFolderPath(fileTypeEnum, id);
        createDirectoryIfNotExists(folderPath);

        File file = new File(folderPath, fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件失败");
        }

        return getRelativePath(fileTypeEnum, id, fileName);
    }

    private String getFolderPath(FileTypeEnum fileTypeEnum, int id) {
//        ApplicationHome home = new ApplicationHome(getClass());
//        File jarFile = home.getSource();
//        String basePath = new File(jarFile.getParentFile(), "ksStatic").getAbsolutePath();

        String basePath = new File(storageRootPath).getAbsolutePath();
        System.out.println(basePath);
        switch (fileTypeEnum) {
            case AVATAR:
                return Paths.get(basePath, "avatar", String.valueOf(id)).toString();
            case FILE:
                return Paths.get(basePath, "file", String.valueOf(id)).toString();
            case DOCUMENT:
                return Paths.get(basePath, "document", String.valueOf(id)).toString();
            default:
                throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
        }
    }

    private String getRelativePath(FileTypeEnum fileTypeEnum, int id, String fileName) {
        return Paths.get("ksStatic", fileTypeEnum.getType(), String.valueOf(id), fileName).toString();
    }

    private void createDirectoryIfNotExists(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败");
        }
    }

//    public String storeTemp(byte[] data, FileTypeEnum fileTypeEnum, int id) {
//        if (data == null) {
//            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "不存在临时文件");
//        }
//
//        String folderPath = getFolderPath(fileTypeEnum, id);
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            if (!folder.mkdirs()) {
//                throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败");
//            }
//        }
//
//        String fileName = UUID.randomUUID().toString();
//        File file = new File(folderPath, fileName);
//        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
//            fileOutputStream.write(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件失败");
//        }
//        return getRelativePath(fileTypeEnum, id, fileName);
//    }
//
//    public String saveFile(MultipartFile file, FileTypeEnum fileTypeEnum, int id) {
//        if (file.isEmpty()) {
//            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件为空");
//        }
//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename == null) {
//            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名为空");
//        }
//
//        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
////        String folderPath = getFolderPath(fileTypeEnum, id);
//
//        String folderPath = getFolderPath(fileTypeEnum, id);
//        System.out.println("Folder path: " + folderPath);
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            if (!folder.mkdirs()) {
//                throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败");
//            }
//        }
//
//        String fileName = UUID.randomUUID().toString() + fileExtension;
//        File destFile = new File(folderPath, fileName);
//        try {
//            file.transferTo(destFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new CommonException(ErrorCode.SYSTEM_ERROR, "保存文件失败");
//        }
//        return getRelativePath(fileTypeEnum, id, fileName);
//    }
//
//    // 创建文件
//    public String createFile(String fileName, String content, FileTypeEnum fileTypeEnum, int id) {
//        if (fileName == null || fileName.isEmpty()) {
//            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名为空");
//        }
//        if (content == null) {
//            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件内容为空");
//        }
//
//        String folderPath = getFolderPath(fileTypeEnum, id);
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            if (!folder.mkdirs()) {
//                throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败");
//            }
//        }
//
//        File file = new File(folderPath, fileName);
//        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
//            fileOutputStream.write(content.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件失败");
//        }
//
//        return getRelativePath(fileTypeEnum, id, fileName);
//    }
//
//    private String getFolderPath(FileTypeEnum fileTypeEnum, int id) {
//        String basePath;
//        //获取jar包所在目录
//        ApplicationHome h = new ApplicationHome(getClass());
//        File jarF = h.getSource();
//        //在jar包所在目录下生成一个upload文件夹用来存储上传的图片
//        switch (fileTypeEnum) {
//            case AVATAR:
//                basePath = resolveResourcePath(jarF.getParentFile().toString(), fileConfig.getAvatarPath());
//                break;
//            case FILE:
//                basePath = resolveResourcePath(jarF.getParentFile().toString(), fileConfig.getFilePath());
//                break;
//            case DOCUMENT:
//                basePath = resolveResourcePath(jarF.getParentFile().toString(), fileConfig.getDocumentPath());
//            default:
//                throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
//        }
//        return Paths.get(basePath, String.valueOf(id)).toString();
//    }
//
//    private String getRelativePath(FileTypeEnum fileTypeEnum, int id, String fileName) {
//        return Paths.get("/", fileTypeEnum.getType(), String.valueOf(id), fileName).toString();
//    }
//
//    private String resolveResourcePath(String basePath, String resourcePath) {
//        try {
//            Resource resource = resourceLoader.getResource("file:" + basePath + resourcePath);
//            if (!resource.exists()) {
//                throw new CommonException(ErrorCode.SYSTEM_ERROR, "资源路径不存在: " + resourcePath);
//            }
//            File file = resource.getFile();
//            Path path = file.toPath();
//            if (!Files.exists(path)) {
//                Files.createDirectories(path);
//            }
//            return path.toString();
//        } catch (IOException e) {
//            throw new CommonException(ErrorCode.SYSTEM_ERROR, "解析资源路径失败: " + e.getMessage());
//        }
//    }
}
