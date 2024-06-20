package com.happlay.ks.service.impl;

import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.File;
import com.happlay.ks.mapper.FileMapper;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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

    @Override
    public String uploadFile(UploadFileRequest uploadFileRequest, Integer userId) {
        MultipartFile file = uploadFileRequest.getFile();
        FileTypeEnum fileType = uploadFileRequest.getFileType();
        String name = uploadFileRequest.getName();
        Integer folderId = uploadFileRequest.getFolderId();

        if (file.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件内容不能为空");
        }

        if (name.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        // 将文件保存到指定位置
        String relativePath = fileUtils.saveFile(file, fileType, folderId);

        File newFile = new File();
        newFile.setFolderId(folderId);
        newFile.setUserId(userId);
        newFile.setName(name);
        newFile.setContent(null);
        newFile.setPath(relativePath);
        newFile.setFileType(fileType.getType());
        newFile.setCreateUser(userId);
        newFile.setUpdateUser(userId);

        this.save(newFile);

        return relativePath;
    }
}
