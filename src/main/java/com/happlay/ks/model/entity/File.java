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
 * 文件表
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file")
@ApiModel(value="File对象", description="文件表")
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "文件夹ID")
    @TableField("folder_id")
    private Integer folderId;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @ApiModelProperty(value = "文件名")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "文件内容，Markdown格式")
    @TableField("content")
    private String content;

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
