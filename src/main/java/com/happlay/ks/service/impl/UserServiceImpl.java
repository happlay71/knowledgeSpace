package com.happlay.ks.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.constant.UserRoleConstant;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.user.LoginUserRequest;
import com.happlay.ks.model.dto.user.RegisterUserRequest;
import com.happlay.ks.model.dto.user.AdminRegisterUserRequest;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.mapper.UserMapper;
import com.happlay.ks.model.vo.user.LoginUserVo;
import com.happlay.ks.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.service.email.VerificationService;
import com.happlay.ks.utils.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
        return this.getById(JwtUtils.getUserIdFromToken(request.getHeader("token")));
    }

    @Override
    public LoginUserVo createLoginUserVo(User user) {
        System.out.println(user);
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtil.copyProperties(user, loginUserVo);
        loginUserVo.setToken(JwtUtils.createToken(user.getId()));
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

        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        this.save(user);

        return createLoginUserVo(user);
    }

    @Override
    public LoginUserVo setUserEmail(String email, User loginUser) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, loginUser.getId());
        User oldUser = this.getOne(queryWrapper);
        if (oldUser != null) {
            oldUser.setEmail(email);
            oldUser.setUpdateUser(loginUser.getId());
            this.updateById(oldUser);
            return createLoginUserVo(oldUser);
        }
        // 创建一个新的 User 对象，用于更新数据库中的记录
        User userToUpdate = new User();
        userToUpdate.setId(loginUser.getId());
        userToUpdate.setEmail(email);

        // 使用 MyBatis Plus 提供的 updateById 方法来更新记录
        this.updateById(userToUpdate);

        // 从数据库中获取更新后的用户信息
        User updatedUser = this.getById(loginUser.getId());

        // 返回更新后的用户信息
        return createLoginUserVo(updatedUser);

    }

    @Override
    public LoginUserVo adminRegisterUser(AdminRegisterUserRequest request, User loginUser) {
        String username = request.getUsername();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        // 保证用户名不重复
        if (this.getOne(queryWrapper) != null) throw new CommonException(ErrorCode.PARAMS_ERROR, "用户名重复");
        // 检查两次密码
        if (!request.getPassword().equals(request.getPassword2()))
            throw new CommonException(ErrorCode.PARAMS_ERROR, "两次密码不一致");

        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        if (request.getRole() != null) {
            if (loginUser.getRole().equals(UserRoleConstant.ROOT)) {
                if (request.getRole().equals(UserRoleConstant.USER_ADMIN))
                    user.setRole(UserRoleConstant.USER_ADMIN);
            } else if (loginUser.getRole().equals(UserRoleConstant.USER_ADMIN)) {
                if (request.getRole().equals(UserRoleConstant.USER_ADMIN) || request.getRole().equals(UserRoleConstant.ROOT)) {
                    throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "权限不足，只能注册普通用户");
                }
            }
        }
        else {
            user.setRole(UserRoleConstant.USER);
        }
        user.setCreateUser(loginUser.getId());
        user.setUpdateUser(loginUser.getId());
        this.save(user);
        return createLoginUserVo(user);
    }
}
