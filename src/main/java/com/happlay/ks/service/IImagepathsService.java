package com.happlay.ks.service;

import com.happlay.ks.model.entity.Imagepaths;
import com.baomidou.mybatisplus.extension.service.IService;
import io.swagger.models.auth.In;

/**
 * <p>
 * 图片路径表 服务类
 * </p>
 *
 * @author happlay
 * @since 2024-06-21
 */
public interface IImagepathsService extends IService<Imagepaths> {

    Boolean saveImageDate(Integer fileId, String imagePath);

    Boolean deleteImage(Integer fileId);

}
