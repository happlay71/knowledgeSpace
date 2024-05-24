package com.happlay.ks.controller;


import com.happlay.ks.service.IUserService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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


    // 增加用户


    // 修改用户信息

    // 删除用户

    // 根据用户名查找用户

    // 分页展示用户
}
