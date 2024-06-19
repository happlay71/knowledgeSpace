package com.happlay.ks.controller;


import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.service.IFileService;
import com.happlay.ks.service.IUserService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

/**
 * <p>
 * 文件表 前端控制器
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    IUserService iUserService;

    @Resource
    IFileService iFileService;

//    // 上传文件
//    public BaseResponse<String> uploadFile(@ModelAttribute UploadFileRequest uploadFileRequest, HttpServletRequest request) {
//        User loginUser = iUserService.getLoginUser(request);
//        return ResultUtils.success(iFileService.)
//    }

}
