package com.happlay.ks.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.happlay.ks.annotation.LoginCheck;
import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.PageRequest;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.model.dto.file.CreateFileRequest;
import com.happlay.ks.model.dto.file.UpdateFileRequest;
import com.happlay.ks.model.dto.file.UpdateNameRequest;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.file.FileDownloadVo;
import com.happlay.ks.model.vo.file.FileVo;
import com.happlay.ks.service.IFileService;
import com.happlay.ks.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
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
@Api(value = "文件")
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

    //    全局模糊搜索文件名
    @GetMapping("/search/file")
    @ApiOperation(value = "查找文件", notes = "传入字符串，页码和页面大小")
    public BaseResponse<Page<FileVo>> searchFiles(
            @RequestParam(required = false) String filename,
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        iUserService.getLoginUser(request);
        Page<FileVo> userVoPage = iFileService.selectFileName(filename, pageRequest);
        return ResultUtils.success(userVoPage);
    }

    @GetMapping("/selectContent")
    @ApiOperation(value = "查看文件内容", notes = "传入查看文件的id")
    public BaseResponse<Map<String, String>> selectFileContent(@RequestParam("id") Integer fileId, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFileService.readFileContent(fileId, loginUser));
    }

    @GetMapping("/down/{id}")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "下载文件", notes = "根据文件ID下载文件")
    public BaseResponse<Map<String, String>> downloadFile(@PathVariable("id") Integer id) throws IOException {
        FileDownloadVo fileDownloadVo = iFileService.downFileById(id);
        org.springframework.core.io.Resource resource = fileDownloadVo.getResource();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDownloadVo.getFileName() + "\"");

        ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

        // 将文件的实际路径返回给前端
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("filePath", resource.getURI().toString());
        responseBody.put("fileName", fileDownloadVo.getFileName());

        return ResultUtils.success(responseBody);
    }
}
