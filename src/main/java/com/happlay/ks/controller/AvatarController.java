package com.happlay.ks.controller;


import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.config.FileConfig;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.user.AvatarUploadVo;
import com.happlay.ks.service.IAvatarService;
import com.happlay.ks.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 头像表 前端控制器
 * </p>
 *
 * @author happlay
 * @since 2024-05-26
 */
@RestController
@RequestMapping("/avatar")
public class AvatarController {

    @Resource
    IAvatarService iAvatarService;
    @Resource
    FileConfig fileConfig;
    @Resource
    IUserService iUserService;

    @PostMapping("/uploadAvatar")
    @ApiOperation(value = "上传头像", notes = "上传图片大小不大于10MB")
    public BaseResponse<AvatarUploadVo> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        if (file.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        // 根据业务需要进行文件验证或其他处理
        try {
            return ResultUtils.success(iAvatarService.uploadAvatar(file, loginUser.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.PARAMS_ERROR, "上传头像失败：" + e.getMessage());
        }
    }
}
