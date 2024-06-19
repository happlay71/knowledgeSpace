package com.happlay.ks.controller;


import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.model.dto.folder.CreateFolderRequest;
import com.happlay.ks.model.dto.folder.UpdateNameRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.folder.FolderVo;
import com.happlay.ks.service.IFolderService;
import com.happlay.ks.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 文件夹表 前端控制器
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@RestController
@RequestMapping("/folder")
public class FolderController {

    @Resource
    IUserService iUserService;

    @Resource
    IFolderService iFolderService;

    @PostMapping("/create")
    @ApiOperation(value = "创建文件夹", notes = "需要用户登录，传入文件名，前端获取登录用户id，父文件夹id（无则不传）")
    public BaseResponse<String> createFolder(@RequestBody CreateFolderRequest cFolderRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFolderService.createFolder(cFolderRequest, loginUser.getId()));
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改文件夹名", notes = "需要用户登录，传入文件id，新文件夹名")
    public BaseResponse<String> updateName(@RequestBody UpdateNameRequest updateNameRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFolderService.updataName(updateNameRequest, loginUser.getId()));
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "删除文件夹", notes = "需要用户登录，传入待删除文件夹id")
    public BaseResponse<Boolean> deleteFolder(@PathVariable("id") Integer id, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFolderService.deleteById(id, loginUser.getId()));
    }

    @GetMapping("/search")
    @ApiOperation(value = "查找该用户的所用文件夹", notes = "需要用户登录，传入用户id")
    public BaseResponse<List<FolderVo>> select(HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFolderService.selectByUserId(loginUser.getId()));
    }
}
