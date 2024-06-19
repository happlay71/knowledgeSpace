package com.happlay.ks.mapper;

import com.happlay.ks.model.entity.Folder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 文件夹表 Mapper 接口
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface FolderMapper extends BaseMapper<Folder> {
    @Delete("DELETE FROM folder WHERE id = #{id}")
    Boolean deleteById(@Param("id") Integer id);

    @Delete("DELETE FROM folder WHERE parent_id = #{parentId}")
    Boolean deleteByParentId(@Param("parentId") Integer parentId);

    @Delete("DELETE FROM folder WHERE user_id = #{userId}")
    Boolean deleteByUserId(@Param("userId") Integer userId);
}
