package com.happlay.ks.utils.file;

import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.config.FileConfig;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

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

    // 保存文件
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
            System.out.println("文件保存成功，路径：" + destFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "保存文件失败");
        }
        return getRelativePath(fileTypeEnum, id, fileName);
    }

    public String saveMarkdownFile(String content, FileTypeEnum fileTypeEnum, int id) {
        String folderPath = getFolderPath(fileTypeEnum, id);
        createDirectoryIfNotExists(folderPath);

        String fileName = UUID.randomUUID().toString() + ".md";
        File file = new File(folderPath, fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Markdown 文件保存成功，路径：" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "保存文件失败");
        }
        return getRelativePath(fileTypeEnum, id, fileName);
    }

    public String saveImage(byte[] imageBytes, int id) {
        String folderPath = getFolderPath(FileTypeEnum.PHOTO, id);
        createDirectoryIfNotExists(folderPath);

        String fileName = UUID.randomUUID().toString() + ".png";
        File imageFile = new File(folderPath, fileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(imageBytes);
            System.out.println("图片保存成功，路径：" + imageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
        return getRelativePath(FileTypeEnum.PHOTO, id, fileName);
    }

    public String deleteFileFromPath(String filePath) {
        String basePath = new File(storageRootPath).getAbsolutePath();
        String resultPath = Paths.get(basePath, filePath).normalize().toString();
        File file = new File(resultPath);

        if (file.exists()) {
            if (file.delete()) {
                return resultPath;
            } else {
                throw new CommonException(ErrorCode.SYSTEM_ERROR, "文件删除失败");
            }
        } else {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件不存在");
        }
    }

    private String getFolderPath(FileTypeEnum fileTypeEnum, int id) {
        String basePath = new File(storageRootPath).getAbsolutePath();
        switch (fileTypeEnum) {
            case AVATAR:
                return Paths.get(basePath, "avatar", String.valueOf(id)).toString();
            case PHOTO:
                return Paths.get(basePath, "document", "photo", String.valueOf(id)).toString();
            case DOCUMENT:
                return Paths.get(basePath, "document", String.valueOf(id)).toString();
            default:
                throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
        }
    }

    private String getRelativePath(FileTypeEnum fileTypeEnum, int id, String fileName) {
        return Paths.get(fileTypeEnum.getType(), String.valueOf(id), fileName).toString();
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
//        createDirectoryIfNotExists(folderPath);
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
//        createDirectoryIfNotExists(folderPath);
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
//        createDirectoryIfNotExists(folderPath);
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
//        String basePath = new File(storageRootPath).getAbsolutePath();
//        switch (fileTypeEnum) {
//            case AVATAR:
//                return Paths.get(basePath, "avatar", String.valueOf(id)).toString();
//            case PHOTO:
//                return Paths.get(basePath, "document", "photo", String.valueOf(id)).toString();
//            case DOCUMENT:
//                return Paths.get(basePath, "document", String.valueOf(id)).toString();
//            default:
//                throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
//        }
//    }
//
//    private String getRelativePath(FileTypeEnum fileTypeEnum, int id, String fileName) {
//        return Paths.get("ksStatic", fileTypeEnum.getType(), String.valueOf(id), fileName).toString();
//    }
//
//    private void createDirectoryIfNotExists(String folderPath) {
//        File folder = new File(folderPath);
//        if (!folder.exists() && !folder.mkdirs()) {
//            throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败");
//        }
//    }
}
