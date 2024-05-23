package com.happlay.ks.service.impl;

import com.happlay.ks.model.entity.File;
import com.happlay.ks.mapper.FileMapper;
import com.happlay.ks.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

}
