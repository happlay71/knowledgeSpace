package com.happlay.ks.utils.imagepaths;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.mapper.ImagepathsMapper;
import com.happlay.ks.model.entity.Imagepaths;
import com.happlay.ks.service.IImagepathsService;
import com.happlay.ks.utils.file.FileUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ImageUtils {
    @Resource
    FileUtils fileUtils;

    @Resource
    IImagepathsService iImagepathsService;

    @Resource
    ImagepathsMapper imagepathsMapper;

    public Boolean saveImageDate(Integer fileId, String imagePath) {
        if (fileId == null || StringUtils.isEmpty(imagePath)) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "缺少请求参数");
        }
        Imagepaths imagepaths = new Imagepaths();
        imagepaths.setFileId(fileId);
        imagepaths.setImagePath(imagePath);

        return iImagepathsService.save(imagepaths);
    }

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
