package com.happlay.ks.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.mapper.FileMapper;
import com.happlay.ks.model.dto.folder.CreateFolderRequest;
import com.happlay.ks.model.dto.folder.UpdateNameRequest;
import com.happlay.ks.model.entity.Folder;
import com.happlay.ks.mapper.FolderMapper;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.folder.FolderVo;
import com.happlay.ks.service.IFolderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 文件夹表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@Service
@Transactional
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements IFolderService {

    @Resource
    FolderMapper folderMapper;

    @Resource
    FileMapper fileMapper;

    @Override
    public String createFolder(CreateFolderRequest createFolderRequest, Integer userId) {
        Folder folder = new Folder();
        if (StringUtils.isEmpty(createFolderRequest.getName()) || createFolderRequest.getName() == null)
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件夹名为空");

        folder.setName(createFolderRequest.getName());
        folder.setUserId(userId);
        folder.setCreateUser(userId);
        folder.setUpdateUser(userId);

        // 先保存文件夹以获取其 ID
        this.save(folder);

        // 如果没有父文件夹，则将 parentId 设置为自身 id
        if (createFolderRequest.getParentId() == 0) {
            folder.setParentId(folder.getId());
        } else {
            LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Folder::getParentId, createFolderRequest.getParentId());
            if (this.getOne(queryWrapper) == null) {
                throw new CommonException(ErrorCode.PARAMS_ERROR, "不存在对应父文件");
            }
            folder.setParentId(createFolderRequest.getParentId());
        }

        this.updateById(folder); // 更新文件夹信息

        return "文件夹创建成功，ID: " + folder.getId();
    }

    @Override
    public String updataName(UpdateNameRequest updateNameRequest, Integer userId) {
        // 使用 LambdaQueryWrapper 根据 ID 查询文件夹
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getId, updateNameRequest.getId());
        Folder folder = this.getOne(queryWrapper);

        // 检查是否找到文件夹
        if (folder == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }

        if (!folder.getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "其他用户禁止修改");
        }

        // 检查新文件夹名是否为空
        if (StringUtils.isEmpty(updateNameRequest.getName()) || updateNameRequest.getName() == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "新文件夹名为空");
        }

        // 更新文件夹名和更新者 ID
        folder.setName(updateNameRequest.getName());
        folder.setUpdateUser(userId);

        // 更新文件夹信息
        this.updateById(folder);

        return "文件夹名称更新成功，ID: " + folder.getId();
    }

    @Override
    public Boolean deleteById(Integer id, Integer userId) {
        // 使用 LambdaQueryWrapper 根据 ID 查询文件夹
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getId, id);
        Folder folder = this.getOne(queryWrapper);

        if (!folder.getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "其他用户禁止删除");
        }
        fileMapper.deleteByFolderId(id);
        folderMapper.deleteByParentId(id);
        return folderMapper.deleteById(id);
    }

    @Override
    public List<FolderVo> selectByUserId(Integer id) {
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getUserId, id).orderByAsc(Folder::getParentId);
        List<Folder> folders = this.list(queryWrapper);

        // 将 Folder 转换为 FolderVo
        List<FolderVo> folderVos = folders.stream().map(folder -> {
            FolderVo folderVo = new FolderVo();
            BeanUtil.copyProperties(folder, folderVo);
            return folderVo;
        }).collect(Collectors.toList());
        return folderVos;
    }
}
