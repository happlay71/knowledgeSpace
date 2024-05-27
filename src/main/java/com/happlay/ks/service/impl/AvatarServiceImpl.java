package com.happlay.ks.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.entity.Avatar;
import com.happlay.ks.mapper.AvatarMapper;
import com.happlay.ks.model.vo.avatar.UploadVo;
import com.happlay.ks.service.IAvatarService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.utils.FileUtils;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * <p>
 * 头像表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-05-26
 */
@Service
@Transactional
public class AvatarServiceImpl extends ServiceImpl<AvatarMapper, Avatar> implements IAvatarService {

    @Resource
    FileUtils fileUtils;

    @Override
    public UploadVo createUploadVo(Avatar avatar) {
        System.out.println(avatar);
        UploadVo uploadVo = new UploadVo();
        BeanUtil.copyProperties(avatar, uploadVo);
        return uploadVo;
    }

    @Override
    public UploadVo uploadAvatar(MultipartFile file, Integer userId) {
        String avatarUrl = fileUtils.saveFile(file, FileTypeEnum.AVATAR, userId);
        // 查询当前用户是否已有头像记录
        LambdaQueryWrapper<Avatar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Avatar::getUserId, userId);
        Avatar old_avatar = this.getOne(queryWrapper);
        if (old_avatar != null) {
            // 删除路径中保存的头像图片及用户 ID 文件夹
            //获取jar包所在目录
            ApplicationHome h = new ApplicationHome(getClass());
            File jarF = h.getSource();
            Path userDir = Paths.get(jarF.getParentFile().toString(), "static", old_avatar.getAvatarUrl()).getParent();
            System.out.println(userDir);
            if (Files.exists(userDir)) {
                try {
                    Files.walk(userDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new CommonException(ErrorCode.SYSTEM_ERROR, "删除旧头像文件夹失败");
                }
            }

            // 更新数据库中当前头像路径信息
            // 更新数据库中当前头像路径信息
            old_avatar.setAvatarUrl(avatarUrl);
            old_avatar.setUpdateUser(userId);
            this.updateById(old_avatar);
            return createUploadVo(old_avatar);
        } else {
            // 如果没有旧头像记录，则创建新的头像记录
            Avatar avatar = new Avatar();
            avatar.setUserId(userId);
            avatar.setAvatarUrl(avatarUrl);
            avatar.setCreateUser(userId);
            avatar.setUpdateUser(userId);
            this.save(avatar);
            return createUploadVo(avatar);
        }

    }
}
