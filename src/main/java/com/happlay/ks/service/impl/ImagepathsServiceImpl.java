package com.happlay.ks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.entity.Imagepaths;
import com.happlay.ks.mapper.ImagepathsMapper;
import com.happlay.ks.service.IImagepathsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.utils.file.FileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 图片路径表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-06-21
 */
@Service
public class ImagepathsServiceImpl extends ServiceImpl<ImagepathsMapper, Imagepaths> implements IImagepathsService {

}
