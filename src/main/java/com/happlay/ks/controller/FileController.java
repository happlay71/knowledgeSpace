package com.happlay.ks.controller;


import com.happlay.ks.annotation.LoginCheck;
import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.constant.UserRoleConstant;
import com.happlay.ks.model.dto.file.CreateFileRequest;
import com.happlay.ks.model.dto.file.UpdateFileRequest;
import com.happlay.ks.model.dto.file.UpdateNameRequest;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.service.IFileService;
import com.happlay.ks.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Map;

import static com.happlay.ks.constant.UserRoleConstant.*;

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
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "上传文件", notes = "需要用户登录，传入文件，文件名")
    public BaseResponse<String> uploadFile(@ModelAttribute UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        String path = iFileService.uploadFile(uploadFileRequest, loginUser);
        return ResultUtils.success(path);
    }

    @PostMapping("/createMD")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "创建md文档", notes = "需要用户登录，传入文件内容，文件名，文件夹id")
    public BaseResponse<Boolean> createMDFile(@RequestBody CreateFileRequest createFileRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFileService.createMDFile(createFileRequest, loginUser));
    }

    // 修改文件名
    @PostMapping("/updateName")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "修改文件名", notes = "需要用户登录，传入待修改的文件id、新文件名")
    public BaseResponse<String> updateFileName(@RequestBody UpdateNameRequest updateNameRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFileService.updateFileName(updateNameRequest, loginUser));
    }

    @PostMapping("/updateContent")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "修改文件内容", notes = "需要用户登录，传入待修改的文件id、文件内容")
    public BaseResponse<String> updateFile(@RequestBody UpdateFileRequest updateFileRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFileService.updateFile(updateFileRequest, loginUser));
    }

    // 删除文件
    @PostMapping("/delete")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "删除文件", notes = "需要用户登录，传入待删除文件的id")
    public BaseResponse<Boolean> deleteFile(@RequestParam("id") Integer id, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFileService.deleteFile(id, loginUser));
    }

    @GetMapping("/selectContent")
    @ApiOperation(value = "查看文件内容", notes = "传入查看文件的id")
    public BaseResponse<Map<String, String>> selectFileContent(@RequestParam("id") Integer fileId, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFileService.readFileContent(fileId, loginUser));
    }
}
