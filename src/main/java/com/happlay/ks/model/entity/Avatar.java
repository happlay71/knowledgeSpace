package com.happlay.ks.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 头像表
 * </p>
 *
 * @author happlay
 * @since 2024-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("avatar")
@ApiModel(value="Avatar对象", description="头像表")
public class Avatar implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "头像ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    @TableField("userId")
    private Integer userId;

    @ApiModelProperty(value = "头像URL")
    @TableField("avatarUrl")
    private String avatarUrl;

    @ApiModelProperty(value = "是否删除")
    @TableField("isDelete")
    private Integer isDelete;

    @ApiModelProperty(value = "创建者")
    @TableField("createUser")
    private Integer createUser;

    @ApiModelProperty(value = "更新者")
    @TableField("updateUser")
    private Integer updateUser;

    @ApiModelProperty(value = "创建时间")
    @TableField("createTime")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("updateTime")
    private LocalDateTime updateTime;


}
