package com.happlay.ks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.File;
import com.happlay.ks.mapper.FileMapper;
import com.happlay.ks.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.service.IImagepathsService;
import com.happlay.ks.utils.file.FileImageUtils;
import com.happlay.ks.utils.file.FileUtils;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@Service
@Transactional
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    @Resource
    FileUtils fileUtils;

    @Resource
    FileImageUtils fileImageUtils;

    @Resource
    IImagepathsService iImagepathsService;

    @Resource
    FileMapper fileMapper;

    @Override
    public String uploadFile(UploadFileRequest uploadFileRequest, Integer userId) {
        MultipartFile file = uploadFileRequest.getFile();
        String originalFilename = file.getOriginalFilename();

        if (file.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件内容不能为空");
        }

        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持该文件类型");
        }

        FileTypeEnum fileType = FileTypeEnum.fromFileName(originalFilename);

        if (fileType != FileTypeEnum.DOCUMENT) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "仅支持文档文件类型");
        }

        String name = uploadFileRequest.getName();
        Integer folderId = uploadFileRequest.getFolderId();

        if (name.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getName, name);
        if (this.getOne(queryWrapper) != null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名不能重复");
        }

        String path;
        try {
            if (originalFilename.endsWith(".md")) {
                path = dealMD(file, folderId, userId, name, fileType);
            } else {
                String relativePath = fileUtils.saveFile(file, fileType, folderId);
                System.out.println("普通文件保存完成，路径：" + relativePath);

                // 保存文件记录到数据库
                File newFile = save(folderId, userId, name, fileType);
                newFile.setPath(relativePath);
                this.updateById(newFile);
                System.out.println("文件信息保存到数据库，路径：" + relativePath);
                path = relativePath;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "处理文件失败");
        }

        return path;
    }

    @Override
    public Boolean deleteFile(Integer id, Integer userId) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getId, id);
        File file = this.getOne(queryWrapper);

        if (!Objects.equals(file.getUserId(), userId)) {
            throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "无权限修改该文件");
        }

        iImagepathsService.deleteImage(id);
        fileUtils.deleteFileFromPath(file.getPath());
        fileMapper.deleteById(id);
        return true;
    }

    public Boolean deleteByFolder(Integer folderId, Integer userId) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getFolderId, folderId);
        List<File> files = fileMapper.selectList(queryWrapper);

        files.forEach(file -> {
            deleteFile(file.getId(), userId);
        });

        return true;
    }


    // 处理Markdown文件
    public String dealMD(MultipartFile file, Integer folderId, Integer userId, String name,
                         FileTypeEnum fileType) throws IOException {
        byte[] fileBytes = file.getBytes();
        System.out.println("开始处理 Markdown 文件");

        // 提取图片路径
        List<String> imagePaths = fileImageUtils.extractImagePathsFromMD(fileBytes);
        System.out.println("提取的图片路径：" + imagePaths);

        File newFile = save(folderId, userId, name, fileType);

        // 获取文件ID
        Integer fileId = newFile.getId();

        // 保存图片路径到数据库并更新图片路径映射
        Map<String, String> imagePathMap = new HashMap<>();
        for (String imagePath : imagePaths) {
            byte[] imageBytes = fileImageUtils.readImage(imagePath);
            String serverPath = fileUtils.saveImage(imageBytes, fileId);
            iImagepathsService.saveImageDate(fileId, serverPath);
            imagePathMap.put(imagePath, serverPath);
        }

        // 更新Markdown内容并保存
        String updatedContent = fileImageUtils.replacePathsInMD(fileBytes, imagePathMap);
        String relativePath = fileUtils.saveMarkdownFile(updatedContent, fileType, folderId);
        newFile.setPath(relativePath);
        this.updateById(newFile); // 更新文件路径
        return relativePath;
    }

    public File save(Integer folderId, Integer userId, String name,
                     FileTypeEnum fileType) {
        // 保存文件记录到数据库
        File newFile = new File();
        newFile.setFolderId(folderId);
        newFile.setUserId(userId);
        newFile.setName(name);
        newFile.setFileType(fileType.getType());
        newFile.setCreateUser(userId);
        newFile.setUpdateUser(userId);
        this.save(newFile);
        return newFile;
    }
}
