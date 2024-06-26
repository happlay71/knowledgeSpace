package com.happlay.ks.mapper;

import com.happlay.ks.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User getById(@Param("id") Integer id);

    @Delete("DELETE FROM user WHERE id = #{id}")
    Boolean deleteById(@Param("id") Integer id);
}
