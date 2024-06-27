package com.happlay.ks.utils.folder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.mapper.FileMapper;
import com.happlay.ks.mapper.FolderMapper;
import com.happlay.ks.service.IImagepathsService;
import com.happlay.ks.utils.imagepaths.ImageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

@Component
public class FolderUtils {

    @Value("${file.storage.root.path}")
    private String storageRootPath;

    @Resource
    ImageUtils imageUtils;

    @Resource
    FolderMapper folderMapper;

    @Resource
    FileMapper fileMapper;

    public String createFolderFromPath(FileTypeEnum fileTypeEnum, int id) {
        String folderPath = getFolderPath(fileTypeEnum, id);
        createDirectoryIfNotExists(folderPath);
        return folderPath;
    }

    // 删除文件夹及其子文件夹和文件
    public Boolean deleteFolderById(FileTypeEnum fileTypeEnum, Integer folderId) {
        // 删除本地文件系统中的文件夹及其内容
        deleteFolderInReal(fileTypeEnum, folderId);

        // 删除数据库中的文件夹及其子文件夹
        deleteFolderAndSubfolders(folderId);
        return true;
    }

    // 获取文件夹的 File 对象
    private File selectFile(FileTypeEnum fileTypeEnum, Integer folderId) {
        String folderPath = getFolderPath(fileTypeEnum, folderId);
        File folder = new File(folderPath);

        if (!folder.exists()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件夹不存在");
        }
        return folder;
    }

    // 删除本地文件系统中的文件夹及其内容
    private Boolean deleteFolderInReal(FileTypeEnum fileTypeEnum, Integer folderId) {
        File folder = selectFile(fileTypeEnum, folderId);

        // 删除文件中保存在本地的图片
        LambdaQueryWrapper<com.happlay.ks.model.entity.File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(com.happlay.ks.model.entity.File::getFolderId, folderId);
        List<com.happlay.ks.model.entity.File> files = fileMapper.selectList(queryWrapper);
        files.forEach(file -> {
            imageUtils.deleteImage(file.getId());
            deleteRecursively(selectFile(FileTypeEnum.PHOTO, file.getId()));
        });

        // 递归删除文件夹及其内容
        deleteRecursively(folder);

        // 获取所有子文件夹并递归删除
        List<Integer> subfolderIds = folderMapper.getSubfolderIds(folderId);
        for (Integer subfolderId : subfolderIds) {
            deleteFolderInReal(fileTypeEnum, subfolderId);
        }

        return true;
    }

    // 删除数据库中的文件夹及其子文件夹
    private void deleteFolderAndSubfolders(Integer folderId) {
        // 获取所有子文件夹
        List<Integer> subfolderIds = folderMapper.getSubfolderIds(folderId);

        // 递归删除子文件夹
        for (Integer subfolderId : subfolderIds) {
            deleteFolderAndSubfolders(subfolderId);
        }

        // 删除当前文件夹
        folderMapper.deleteById(folderId);
    }

    // 递归删除文件和文件夹
    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    deleteRecursively(subFile);
                }
            }
        }

        if (!file.delete()) {
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "文件删除失败: " + file.getAbsolutePath());
        }
    }

    // 获取文件夹的路径
    public String getFolderPath(FileTypeEnum fileTypeEnum, int id) {
        String basePath = new File(storageRootPath).getAbsolutePath();
        switch (fileTypeEnum) {
            case AVATAR:
                return Paths.get(basePath, "avatar", String.valueOf(id)).normalize().toString();
            case PHOTO:
                return Paths.get(basePath, "document", "photo", String.valueOf(id)).normalize().toString();
            case DOCUMENT:
                return Paths.get(basePath, "document", String.valueOf(id)).normalize().toString();
            case BOOKMARK:
                return Paths.get(basePath, "document", "bookMark", String.valueOf(id)).normalize().toString();
            default:
                throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
        }
    }

    // 获取相对路径
    private String getRelativePath(FileTypeEnum fileTypeEnum, int id, String fileName) {
        return Paths.get(fileTypeEnum.getType(), String.valueOf(id), fileName).toString();
    }

    // 如果文件夹不存在则创建
    private void createDirectoryIfNotExists(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败");
        }
    }
}
