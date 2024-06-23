package com.happlay.ks.service;

import com.happlay.ks.model.dto.folder.CreateFolderRequest;
import com.happlay.ks.model.dto.folder.UpdateNameRequest;
import com.happlay.ks.model.entity.Folder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.folder.FolderVo;

import java.util.List;

/**
 * <p>
 * 文件夹表 服务类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface IFolderService extends IService<Folder> {

    Boolean folderBelongsToUser(Integer folderId, Integer userId);

    String createFolder(CreateFolderRequest createFolderRequest, User user);

    String updataName(UpdateNameRequest updateNameRequest, User user);

    Boolean deleteById(Integer id, User user);

    List<FolderVo> selectByUserId(Integer id);
}
