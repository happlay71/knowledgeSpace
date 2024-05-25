package com.happlay.ks.controller;


import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.user.LoginUserRequest;
import com.happlay.ks.model.dto.user.RegisterUserRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.LoginUserVo;
import com.happlay.ks.service.IUserService;
import com.happlay.ks.service.email.EmailService;
import com.happlay.ks.service.email.VerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.transform.Result;

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
    @ApiOperation(value = "注册", notes = "传入用户名，密码，确认密码，默认用户为普通用户")
    public BaseResponse<LoginUserVo> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResultUtils.success(iUserService.register(request));
    }

    @PostMapping("/sendVerificationEmail")
    @ApiOperation(value = "发送邮件", notes = "传入邮箱账号")
    public BaseResponse<String> sendVerificationEmail(@RequestParam String email) {
        verificationService.sendVerificationEmail(email);
        return ResultUtils.success("验证码发送成功");
    }

    @PostMapping("/verifyCode")
    @ApiOperation(value = "验证邮箱信息", notes = "传入对应的邮箱和验证码")
    public BaseResponse<String> verifyCode(@RequestParam String email, @RequestParam String code, HttpServletRequest request) {
        User loginUser = iUserService.getLoginUser(request);
        verificationService.verifyCode(email, code);
        loginUser.setEmail(email);
        iUserService.save(loginUser);
        return ResultUtils.success("邮箱验证成功");
    }
    // 增加用户


    // 修改用户信息

    // 删除用户

    // 根据用户名查找用户

    // 分页展示用户
}
