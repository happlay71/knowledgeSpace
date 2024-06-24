package com.happlay.ks.controller;


import com.happlay.ks.annotation.LoginCheck;
import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.model.dto.folder.CreateFolderRequest;
import com.happlay.ks.model.dto.folder.UpdateNameRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.file.FileDetailsVo;
import com.happlay.ks.model.vo.folder.FolderDetailsVo;
import com.happlay.ks.model.vo.folder.FolderVo;
import com.happlay.ks.service.IFileService;
import com.happlay.ks.service.IFolderService;
import com.happlay.ks.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.happlay.ks.constant.UserRoleConstant.*;

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

    @Resource
    IFileService iFileService;

    @PostMapping("/create")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "创建文件夹", notes = "需要用户登录，传入文件名，前端获取登录用户id，父文件夹id（无则不传）")
    public BaseResponse<String> createFolder(@RequestBody CreateFolderRequest cFolderRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFolderService.createFolder(cFolderRequest, loginUser, true));
    }

    @PostMapping("/update")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "修改文件夹名", notes = "需要用户登录，传入文件id，新文件夹名")
    public BaseResponse<String> updateName(@RequestBody UpdateNameRequest updateNameRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFolderService.updataName(updateNameRequest, loginUser));
    }

    @PostMapping("/delete/{id}")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "删除文件夹", notes = "需要用户登录，传入待删除文件夹id")
    public BaseResponse<Boolean> deleteFolder(@PathVariable("id") Integer id, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iFolderService.deleteById(id, loginUser, true));
    }

//    @GetMapping("/search")
//    @ApiOperation(value = "查找该用户的所有文件夹", notes = "需要用户登录，传入用户id")
//    public BaseResponse<FolderDetailsVo> select(HttpServletRequest request) {
//        User loginUser = iUserService.getLoginUser(request);
//        return ResultUtils.success(iFolderService.getFolderStructureByUserId(loginUser.getId()));
//    }

    @GetMapping("/select/{id}")
    @ApiOperation(value = "查看文件夹的子文件夹和文件", notes = "需要用户登录，传入文件夹id")
    public BaseResponse<FolderDetailsVo> selectById(@PathVariable("id") Integer id, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);

        FolderDetailsVo folderDetailsVo;
//        //暂时不禁止访客查看
//        if (GUEST.equals(loginUser.getRole())) {
//            // 访客只能查看有限的信息
//            // 获取文件夹结构
//            folderDetailsVo = iFolderService.getFolderStructureByUserId(id);
//            // 获取文件内容
//            iFileService.addFilesToFolders(folderDetailsVo, false);
//        } else {
//            // 已登录用户可以查看详细信息
//            // 获取文件夹结构
//            folderDetailsVo = iFolderService.getFolderStructureByUserId(id);
//            // 获取文件内容
//            iFileService.addFilesToFolders(folderDetailsVo, true);
//        }

        // 获取文件夹结构
        folderDetailsVo = iFolderService.getFolderStructureByUserId(id);
        // 获取文件内容
        iFileService.addFilesToFolders(folderDetailsVo, true);
        return ResultUtils.success(folderDetailsVo);
    }


//    private void addFilesToFolders(FolderDetailsVo folderDetailsVo, boolean isLoggedIn) {
//        List<FileDetailsVo> files = iFileService.getFilesByFolderId(folderDetailsVo.getId(), isLoggedIn);
//        folderDetailsVo.setFiles(files);
//        for (FolderDetailsVo subFolder : folderDetailsVo.getSubFolders()) {
//            addFilesToFolders(subFolder, isLoggedIn);
//        }
//    }
}
