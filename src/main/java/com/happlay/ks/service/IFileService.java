package com.happlay.ks.service;

import com.happlay.ks.model.dto.file.CreateFileRequest;
import com.happlay.ks.model.dto.file.UpdateFileRequest;
import com.happlay.ks.model.dto.file.UpdateNameRequest;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;
import com.happlay.ks.model.entity.Folder;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.file.FileDetailsVo;
import com.happlay.ks.model.vo.folder.FolderDetailsVo;

import java.util.List;
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

    String uploadFile(UploadFileRequest uploadFileRequest, User user);

    Boolean createMDFile(CreateFileRequest createFileRequest, User user);

    Boolean deleteFile(Integer id, User user);

    Boolean deleteByFolder(Integer folderId, User user);

    String updateFileName(UpdateNameRequest updateNameRequest, User user);

    String updateFile(UpdateFileRequest updateFileRequest, User user);

    String selectFileContent(Integer fileId, User user);

//    FolderDetailsVo convertToFolderDetailsVo(Folder folder, boolean isLoggedIn);

    List<FileDetailsVo> getFilesByFolderId(Integer folderId, boolean isLoggedIn);

    void addFilesToFolders(FolderDetailsVo folderDetailsVo, boolean isLoggedIn);
}