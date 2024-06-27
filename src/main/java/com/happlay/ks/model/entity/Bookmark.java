package com.happlay.ks.model.entity;

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
 * 书签表
 * </p>
 *
 * @author happlay
 * @since 2024-06-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bookmark")
@ApiModel(value="Bookmark对象", description="书签表")
public class Bookmark implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "书签ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "书签所在文件夹ID")
    private Integer folderId;

    @ApiModelProperty(value = "书签标题")
    private String title;

    @ApiModelProperty(value = "书签URL")
    private String url;

    @ApiModelProperty(value = "创建者")
    private Integer createUser;

    @ApiModelProperty(value = "更新者")
    private Integer updateUser;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
