package com.happlay.ks.mapper;

import com.happlay.ks.model.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 文件表 Mapper 接口
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface FileMapper extends BaseMapper<File> {

    @Delete("DELETE FROM file WHERE id = #{id}")
    Boolean deleteById(@Param("id") Integer id);
//
//    @Delete("DELETE FROM file WHERE folder_id = #{folderId}")
//    Boolean deleteByFolderId(@Param("folderId") Integer folderId);
//
//    @Delete("DELETE FROM file WHERE user_id = #{userId}")
//    Boolean deleteByUserId(@Param("userId") Integer userId);
}
