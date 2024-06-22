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

    @Resource
    FileUtils fileUtils;

    @Resource
    ImagepathsMapper imagepathsMapper;

    @Override
    public Boolean saveImageDate(Integer fileId, String imagePath) {
        if (fileId == null || StringUtils.isEmpty(imagePath)) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "缺少请求参数");
        }
        Imagepaths imagepaths = new Imagepaths();
        imagepaths.setFileId(fileId);
        imagepaths.setImagePath(imagePath);

        return this.save(imagepaths);
    }

    @Override
    public Boolean deleteImage(Integer fileId) {
        LambdaQueryWrapper<Imagepaths> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Imagepaths::getFileId, fileId);
        List<Imagepaths> imagepathsList = imagepathsMapper.selectList(queryWrapper);

        // 使用forEach代替map和collect
        imagepathsList.forEach(imagePath -> {
            try {
                // 删除物理文件
                fileUtils.deleteFileFromPath(imagePath.getImagePath());
                // 从数据库中删除记录
                imagepathsMapper.deleteById(imagePath.getId());
                // 日志记录
                System.out.println("Deleted file and database record for: " + imagePath.getImagePath());
            } catch (Exception e) {
                // 处理异常并记录错误信息
                System.err.println("Error deleting file or database record for: " + imagePath.getImagePath());
                e.printStackTrace();
            }
        });

        return true;
    }
}
