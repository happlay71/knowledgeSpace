package com.happlay.ks.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.common.ResultUtils;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.user.LoginUserRequest;
import com.happlay.ks.model.dto.user.RegisterUserRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.mapper.UserMapper;
import com.happlay.ks.model.vo.LoginUserVo;
import com.happlay.ks.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.service.email.EmailService;
import com.happlay.ks.service.email.VerificationService;
import com.happlay.ks.utils.JWTUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.beans.Transient;
import java.util.Objects;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    VerificationService verificationService;

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return this.getById(JWTUtils.getUserIdFromToken(request.getHeader("token")));
    }

    @Override
    public LoginUserVo createLoginUserVo(User user) {
        System.out.println(user);
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtil.copyProperties(user, loginUserVo);
        loginUserVo.setToken(JWTUtils.createToken(user.getId()));
        return loginUserVo;
    }

    @Override
    public LoginUserVo login(LoginUserRequest request) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername())
                .eq(User::getPassword, request.getPassword());
        User user = this.getOne(queryWrapper, true);
        if (user == null) throw new CommonException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        return createLoginUserVo(user);
    }

    @Override
    public LoginUserVo register(RegisterUserRequest request) {
        String username = request.getUsername();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        // 保证用户名不重复
        if (this.getOne(queryWrapper) != null) throw new CommonException(ErrorCode.PARAMS_ERROR, "用户名重复");
        // 检查两次密码
        if (!request.getPassword().equals(request.getPassword2()))
            throw new CommonException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
//        // 检查邮箱是否已注册
//        queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(User::getEmail, request.getEmail());
//        if (this.getOne(queryWrapper) != null)
//            throw new CommonException(ErrorCode.PARAMS_ERROR, "邮箱已注册");
//        // 发送邮件
//        verificationService.sendVerificationEmail(request.getEmail());
        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        this.save(user);

        return createLoginUserVo(user);
    }
}
