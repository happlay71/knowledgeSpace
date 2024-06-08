package com.happlay.ks.controller;


import com.happlay.ks.annotation.LoginCheck;
import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.constant.UserRoleConstant;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.user.LoginUserRequest;
import com.happlay.ks.model.dto.user.RegisterUserRequest;
import com.happlay.ks.model.dto.user.AdminRegisterUserRequest;
import com.happlay.ks.model.dto.user.UpdateUserRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.user.AvatarUploadVo;
import com.happlay.ks.model.vo.user.LoginUserVo;
import com.happlay.ks.service.IUserService;
import com.happlay.ks.service.email.VerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@RestController
@RequestMapping("/user")
@Api(value = "用户")
public class UserController {
    @Resource
    IUserService iUserService;
    @Resource
    VerificationService verificationService;

    @PostMapping("/sendVerificationEmail")
    @ApiOperation(value = "发送邮件", notes = "传入邮箱账号")
    public BaseResponse<String> sendVerificationEmail(@RequestParam String email, HttpServletRequest request) {
        iUserService.getLoginUser(request);
        verificationService.sendVerificationEmail(email);
        return ResultUtils.success("验证码发送成功");
    }

    @PostMapping("/verifyCode")
    @ApiOperation(value = "验证邮箱信息，设置邮箱", notes = "传入对应的邮箱和验证码")
    public BaseResponse<LoginUserVo> verifyCode(@RequestParam String email, @RequestParam String code, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        verificationService.verifyCode(email, code);
        return ResultUtils.success(iUserService.setUserEmail(email, loginUser));
    }

    @PostMapping("/login")
    @ApiOperation(value = "登录", notes = "传入用户名，密码")
    public BaseResponse<LoginUserVo> login(@RequestBody LoginUserRequest request) {
        return ResultUtils.success(iUserService.login(request));
    }

    /**
     * @Valid 用于验证加上了@NotBlank和@Email的属性
     * @param request
     * @return
     */
    @PostMapping("/register")
    @LoginCheck(mustRole = {UserRoleConstant.ROOT})
    @ApiOperation(value = "注册", notes = "传入用户名，密码，确认密码，默认用户为普通用户，目前仅由超级管理员注册")
    public BaseResponse<LoginUserVo> register(@RequestBody RegisterUserRequest request) {
        return ResultUtils.success(iUserService.register(request));
    }

    @PostMapping("/setAvatar")
    @ApiOperation(value = "上传头像", notes = "上传图片大小不大于10MB")
    public BaseResponse<AvatarUploadVo> setAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        if (file.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        // 根据业务需要进行文件验证或其他处理
        try {
            return ResultUtils.success(iUserService.uploadAvatar(file, loginUser.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.PARAMS_ERROR, "上传头像失败：" + e.getMessage());
        }
    }

    @PostMapping("/register/admin")
    @LoginCheck(mustRole = {UserRoleConstant.ROOT, UserRoleConstant.USER_ADMIN})
    @ApiOperation(value = "超级管理员或管理员添加用户", notes = "传入用户名，密码，确认密码，角色role")
    public BaseResponse<LoginUserVo> addUserByAdmin(@RequestBody AdminRegisterUserRequest request, HttpServletRequest servletRequest) {
        User loginUser = iUserService.getLoginUser(servletRequest);
        return ResultUtils.success(iUserService.adminRegisterUser(request, loginUser));
    }

    @PostMapping("/delete/me")
    @ApiOperation(value = "删除用户(自己)", notes = "需登录")
    public BaseResponse<Boolean> deleteMe(HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iUserService.removeAllById(loginUser, loginUser));
    }

    //
    @PostMapping("/delete/{id}")
    @LoginCheck(mustRole = {UserRoleConstant.ROOT, UserRoleConstant.USER_ADMIN})
    @ApiOperation(value = "删除用户(管理员)", notes = "id通过url传递，只有管理员可操作")
    public BaseResponse<Boolean> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        User user = iUserService.getById(id);
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iUserService.removeAllById(user, loginUser));
    }

    // 重置密码
    public BaseResponse<Boolean> resetPassword(HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iUserService.resetPassword(loginUser));
    }

    @PostMapping("/update/me")
    @ApiOperation(value = "用户修改姓名密码", notes = "传入新用户名，密码，确认密码，需要用户登录")
    public BaseResponse<Boolean> updateMe(@RequestBody UpdateUserRequest updateUserRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        updateUserRequest.setId(loginUser.getId());
        return ResultUtils.success(iUserService.update(updateUserRequest, loginUser));
    }

    @PostMapping("/update")
    @LoginCheck(mustRole = {UserRoleConstant.ROOT, UserRoleConstant.USER_ADMIN})
    @ApiOperation(value = "修改用户信息", notes = "id通过请求体传递,只有管理员可操作")
    public BaseResponse<Boolean> update(@RequestBody UpdateUserRequest updateUserRequest, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iUserService.update(updateUserRequest, loginUser));
    }
    // 根据用户名查找用户
//    @GetMapping("/search")
//    public BaseResponse<Page<UserVo>> searchUsers(
//            @RequestParam(required = false) String username,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return userService.searchUsers(username, page, size);
//    }

    // 分页查找用户
}
