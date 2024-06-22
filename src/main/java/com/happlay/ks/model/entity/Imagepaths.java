package com.happlay.ks.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 图片路径表
 * </p>
 *
 * @author happlay
 * @since 2024-06-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("imagepaths")
@ApiModel(value="Imagepaths对象", description="图片路径表")
public class Imagepaths implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "图片路径ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "文件ID")
    @TableField("file_id")
    private Integer fileId;

    @ApiModelProperty(value = "图片路径")
    @TableField("image_path")
    private String imagePath;


}
