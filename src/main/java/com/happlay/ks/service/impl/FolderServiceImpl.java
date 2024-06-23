package com.happlay.ks.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.constant.UserRoleConstant;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.mapper.FileMapper;
import com.happlay.ks.model.dto.folder.CreateFolderRequest;
import com.happlay.ks.model.dto.folder.UpdateNameRequest;
import com.happlay.ks.model.entity.Folder;
import com.happlay.ks.mapper.FolderMapper;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.folder.FolderVo;
import com.happlay.ks.service.IFileService;
import com.happlay.ks.service.IFolderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.utils.file.FileUtils;
import com.happlay.ks.utils.file.FolderUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    FolderUtils folderUtils;

    @Resource
    FolderMapper folderMapper;

    @Override
    public Boolean folderBelongsToUser(Integer folderId, Integer userId) {
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getId, folderId)
                .eq(Folder::getUserId, userId);
        return folderMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public String createFolder(CreateFolderRequest createFolderRequest, User user) {
        // 检查文件夹是否存在并属于当前用户
        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                ||  Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN))
                && !folderBelongsToUser(createFolderRequest.getParentId(), user.getId())) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "操作无效，无权创建");
        }

        Folder folder = new Folder();
        if (StringUtils.isEmpty(createFolderRequest.getName()) || createFolderRequest.getName() == null)
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件夹名为空");

        LambdaQueryWrapper<Folder> oldQueryWrapper = new LambdaQueryWrapper<>();
        oldQueryWrapper.eq(Folder::getName, createFolderRequest.getName());
        if (this.getOne(oldQueryWrapper) != null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件夹名不能重复");
        }

        folder.setName(createFolderRequest.getName());
        folder.setUserId(user.getId());
        folder.setCreateUser(user.getId());
        folder.setUpdateUser(user.getId());

        // 先保存文件夹以获取其 ID
        this.save(folder);

        // 如果没有父文件夹，则将 parentId 设置为自身 id
        if (createFolderRequest.getParentId() == 0) {
            LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Folder::getId, createFolderRequest.getParentId());
            if (this.getOne(queryWrapper) != null) {
                throw new CommonException(ErrorCode.PARAMS_ERROR, "存在对应父文件");
            }
            folder.setParentId(0);
        } else {
            LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Folder::getId, createFolderRequest.getParentId());
            if (this.getOne(queryWrapper) == null) {
                throw new CommonException(ErrorCode.PARAMS_ERROR, "不存在对应父文件");
            }
            folder.setParentId(createFolderRequest.getParentId());
        }

        this.updateById(folder); // 更新文件夹信息

        // 物理上创建文件夹
        String folderFromPath = folderUtils.createFolderFromPath(FileTypeEnum.DOCUMENT, folder.getId());
        return "文件夹创建成功，ID: " + folder.getId() + "path: " + folderFromPath;
    }

    @Override
    public String updataName(UpdateNameRequest updateNameRequest, User user) {
        // 检查文件夹是否存在并属于当前用户
        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                ||  Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN))
                && !folderBelongsToUser(updateNameRequest.getId(), user.getId())) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "操作无效, 其他用户禁止修改");
        }

        // 使用 LambdaQueryWrapper 根据 ID 查询文件夹
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getId, updateNameRequest.getId());
        Folder folder = this.getOne(queryWrapper);

        // 检查是否找到文件夹
        if (folder == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }

        // 检查新文件夹名是否为空
        if (StringUtils.isEmpty(updateNameRequest.getName()) || updateNameRequest.getName() == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "新文件夹名为空");
        }

        if (this.getOne(new LambdaQueryWrapper<Folder>().eq(Folder::getName, updateNameRequest.getName())) != null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件夹名不能重复");
        }

        // 更新文件夹名和更新者 ID
        folder.setName(updateNameRequest.getName());
        folder.setUpdateUser(user.getId());

        // 更新文件夹信息
        this.updateById(folder);

        return "文件夹名称更新成功，ID: " + folder.getId();
    }

    @Override
    public Boolean deleteById(Integer id, User user) {
        // 使用 LambdaQueryWrapper 根据 ID 查询文件夹
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getId, id);
        Folder folder = this.getOne(queryWrapper);

        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                ||  Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN))
                && folder == null || !folder.getUserId().equals(user.getId())) {
            throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "其他用户禁止删除");
        }

        // 递归删除文件夹及其子文件夹和文件
//        deleteFolderRecursively(id, userId);
        folderUtils.deleteFolderById(FileTypeEnum.DOCUMENT, id);
        return folderMapper.deleteById(id);
    }

//    private void deleteFolderRecursively(Integer folderId, Integer userId) {
//        // 查询文件夹下的所有子文件夹
//        LambdaQueryWrapper<Folder> folderQueryWrapper = new LambdaQueryWrapper<>();
//        folderQueryWrapper.eq(Folder::getParentId, folderId);
//        List<Folder> subFolders = this.list(folderQueryWrapper);
//
//        // 删除子文件夹及其内容
//        for (Folder subFolder : subFolders) {
//            deleteFolderRecursively(subFolder.getId(), userId);
//        }
//
//        // 删除当前文件夹下的所有文件
//        iFileService.deleteByFolder(folderId, userId);
//
//        // 删除当前文件夹---数据库
//        folderMapper.deleteById(folderId);
//    }

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
