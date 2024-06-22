package com.happlay.ks.controller;


import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.service.IFileService;
import com.happlay.ks.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/upload")
    @ApiOperation(value = "上传文件", notes = "需要用户登录，传入文件，文件名")
    public BaseResponse<String> uploadFile(@ModelAttribute UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        String path = iFileService.uploadFile(uploadFileRequest, loginUser.getId());
        return ResultUtils.success(path);
    }

    // 创建md文档

    // 修改文件内容

    // 删除文件
    @PostMapping("/delete")
    @ApiOperation(value = "删除文件", notes = "需要用户登录，传入待删除文件的id")
    public BaseResponse<Boolean> deleteFile(@RequestParam("id") Integer id, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFileService.deleteFile(id, loginUser.getId()));
    }

    // 查看文件内容
}
