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

    String createFolder(CreateFolderRequest createFolderRequest, Integer userId);

    String updataName(UpdateNameRequest updateNameRequest, Integer userId);

    Boolean deleteById(Integer id, Integer userId);

    List<FolderVo> selectByUserId(Integer id);
}
