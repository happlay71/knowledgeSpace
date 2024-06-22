package com.happlay.ks.service;

import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;
import com.happlay.ks.model.entity.User;

import java.util.Map;

/**
 * <p>
 * 文件表 服务类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface IFileService extends IService<File> {

    String uploadFile(UploadFileRequest uploadFileRequest, Integer userId);

    Boolean deleteFile(Integer id, Integer userId);

    Boolean deleteByFolder(Integer folderId, Integer userId);

}