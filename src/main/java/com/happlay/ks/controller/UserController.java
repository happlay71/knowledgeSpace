package com.happlay.ks.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.happlay.ks.annotation.LoginCheck;
import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.common.PageRequest;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.constant.UserRoleConstant;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.email.VerifyCodeRequest;
import com.happlay.ks.model.dto.user.*;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.user.AvatarUploadVo;
import com.happlay.ks.model.vo.user.LoginUserVo;
import com.happlay.ks.model.vo.user.UserDetailsVo;
import com.happlay.ks.model.vo.user.UserVo;
import com.happlay.ks.service.IUserService;
import com.happlay.ks.service.email.VerificationService;
import com.happlay.ks.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.happlay.ks.constant.UserRoleConstant.*;

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

    @PostMapping("/sendVerificationEmailForSetEmail")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "发送邮件（设置邮箱）", notes = "传入邮箱账号")
    public BaseResponse<String> sendVerificationEmailForSetEmail(@RequestParam("email") String email, HttpServletRequest request) {
        iUserService.getLoginUser(request);
        verificationService.sendVerificationEmail(email);
        return ResultUtils.success("验证码发送成功");
    }

    @PostMapping("/sendVerificationEmailForResetPassword")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "发送邮件（重置密码）", notes = "传入邮箱账号")
    public BaseResponse<String> sendVerificationEmailForResetPassword(@RequestParam("email") String email) {
        verificationService.sendVerificationEmail(email);
        return ResultUtils.success("验证码发送成功");
    }

    @PostMapping("/verifyCode")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "验证邮箱信息", notes = "传入对应的邮箱(前端传入)和验证码, 生成Token")
    public BaseResponse<String> verifyCode(VerifyCodeRequest verifyCodeRequest) {

        boolean isVerified = verificationService.verifyCode(verifyCodeRequest.getEmail(), verifyCodeRequest.getCode());
        if (isVerified) {
            // 生成短时间有效的 JWT 令牌
            String token = JwtUtils.createEmailToken(verifyCodeRequest.getEmail());
            return ResultUtils.success(token);
        } else {
            return ResultUtils.error("邮箱验证失败");
        }
    }

    @PostMapping("/setEmail")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "设置邮箱", notes = "传入邮箱, emailToken 通过请求头传递")
    public BaseResponse<LoginUserVo> setEmail(@RequestParam("email") String email,
                                              @RequestHeader("emailToken") String emailToken,
                                              HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);

        // 有点多于
        if (!email.equals(JwtUtils.getEmailFromToken(emailToken))) {
            throw new CommonException(ErrorCode.TOKEN_ERROR, "邮箱与emailToken不符");
        }
        return ResultUtils.success(iUserService.setUserEmail(email, loginUser));
    }

    @PostMapping("/login")
    @ApiOperation(value = "登录(访客）", notes = "传入用户名，密码")
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
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
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

    // 删除头像
    @PostMapping("/delAvatar")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "删除头像", notes = "需要用户登录")
    public BaseResponse<Boolean> delAvatar(HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iUserService.deleteAvatar(loginUser));
    }

    @PostMapping("/register/admin")
    @LoginCheck(mustRole = {UserRoleConstant.ROOT, UserRoleConstant.USER_ADMIN})
    @ApiOperation(value = "超级管理员或管理员添加用户", notes = "传入用户名，密码，确认密码，角色role")
    public BaseResponse<LoginUserVo> addUserByAdmin(@RequestBody AdminRegisterUserRequest request, HttpServletRequest servletRequest) {
        User loginUser = iUserService.getLoginUser(servletRequest);
        return ResultUtils.success(iUserService.adminRegisterUser(request, loginUser));
    }

    @PostMapping("/delete/me")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "删除用户(自己)", notes = "需登录")
    public BaseResponse<Boolean> deleteMe(HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iUserService.removeAllById(loginUser, loginUser));
    }

    @PostMapping("/delete/{id}")
    @LoginCheck(mustRole = {UserRoleConstant.ROOT, UserRoleConstant.USER_ADMIN})
    @ApiOperation(value = "删除用户(管理员)", notes = "id通过url传递，只有管理员可操作")
    public BaseResponse<Boolean> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        User user = iUserService.getById(id);
        User loginUser = iUserService.getLoginUser(request);
        return ResultUtils.success(iUserService.removeAllById(user, loginUser));
    }

    @PostMapping("/resetPassword")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
    @ApiOperation(value = "重置密码", notes = "验证完邮箱后，传入密码，二次密码")
    public BaseResponse<Boolean> resetPassword(@RequestHeader("emailToken") String emailToken,
                                               ResetUserPasswordRequest resetRequest) {
        String email = JwtUtils.getEmailFromToken(emailToken);
        return ResultUtils.success(iUserService.resetPassword(email, resetRequest));
    }

    @PostMapping("/update/me")
    @LoginCheck(mustRole = {ROOT, USER_ADMIN, USER})
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

    @GetMapping("/search")
    @ApiOperation(value = "用户查询（模糊匹配）", notes = "传入字符串，页码和页面大小")
    public BaseResponse<Page<UserVo>> searchUsers(
            @RequestParam(required = false) String username,
            PageRequest pageRequest,
            HttpServletRequest request) {
        iUserService.getLoginUser(request);
        Page<UserVo> userVoPage = iUserService.selectName(username, pageRequest);
        return ResultUtils.success(userVoPage);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询", notes = "传入页码和页面大小")
    public BaseResponse<Page<UserVo>> selectPage(PageRequest pageRequest, HttpServletRequest request) {
        iUserService.getLoginUser(request);
        Page<UserVo> userVoPage = iUserService.selectPage(pageRequest);
        return ResultUtils.success(userVoPage);
    }

    @GetMapping("/select/me")
    @LoginCheck(mustRole = {UserRoleConstant.ROOT, UserRoleConstant.USER_ADMIN, UserRoleConstant.USER})
    @ApiOperation(value = "登陆后查看自己的文件", notes = "需要用户登录")
    public BaseResponse<UserDetailsVo> selectMe(HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        UserDetailsVo userDetailsVo = iUserService.getUserDetailsById(loginUser.getId(), true);
        return ResultUtils.success(userDetailsVo);
    }

    @GetMapping("/select/{id}")
    @ApiOperation(value = "根据用户ID查询（登录/访客）", notes = "前端传入用户ID")
    public BaseResponse<UserDetailsVo> selectByIdUser(@PathVariable("id") Integer id, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        UserDetailsVo userDetailsVo;

        if (GUEST.equals(loginUser.getRole())) {
            // 访客只能查看有限的信息
            userDetailsVo = iUserService.getUserDetailsById(id, false);
        } else {
            // 已登录用户可以查看详细信息
            userDetailsVo = iUserService.getUserDetailsById(id, true);
        }

        return ResultUtils.success(userDetailsVo);
    }

}
