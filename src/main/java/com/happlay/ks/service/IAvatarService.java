package com.happlay.ks.service;

import com.happlay.ks.model.entity.Avatar;
import com.baomidou.mybatisplus.extension.service.IService;
import com.happlay.ks.model.vo.avatar.UploadVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 头像表 服务类
 * </p>
 *
 * @author happlay
 * @since 2024-05-26
 */
public interface IAvatarService extends IService<Avatar> {

    UploadVo createUploadVo(Avatar avatar);
    UploadVo uploadAvatar(MultipartFile file, Integer userId);
}
