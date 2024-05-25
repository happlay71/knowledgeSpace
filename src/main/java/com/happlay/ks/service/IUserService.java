package com.happlay.ks.service;

import com.happlay.ks.model.dto.user.LoginUserRequest;
import com.happlay.ks.model.dto.user.RegisterUserRequest;
import com.happlay.ks.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.happlay.ks.model.vo.LoginUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface IUserService extends IService<User> {

    User getLoginUser(HttpServletRequest request);
    LoginUserVo createLoginUserVo(User user);
    LoginUserVo login(LoginUserRequest request);
    LoginUserVo register(RegisterUserRequest request);

}
