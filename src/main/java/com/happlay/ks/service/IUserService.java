package com.happlay.ks.service;

import com.happlay.ks.model.dto.user.LoginUserRequest;
import com.happlay.ks.model.dto.user.RegisterUserRequest;
import com.happlay.ks.model.dto.user.AdminRegisterUserRequest;
import com.happlay.ks.model.dto.user.UpdateUserRequest;
import com.happlay.ks.model.entity.Avatar;
import com.happlay.ks.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.happlay.ks.model.vo.user.AvatarUploadVo;
import com.happlay.ks.model.vo.user.LoginUserVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface IUserService extends IService<User> {

    LoginUserVo register(RegisterUserRequest request);
    LoginUserVo setUserEmail(String email, User loginUser);
    User getLoginUser(HttpServletRequest request);
    AvatarUploadVo createUploadVo(User avatar);
    AvatarUploadVo uploadAvatar(MultipartFile file, Integer userId);
    LoginUserVo createLoginUserVo(User user);
    LoginUserVo login(LoginUserRequest request);
    LoginUserVo adminRegisterUser(AdminRegisterUserRequest request, User loginUser);
    Boolean removeAllById(User user, User loginUser);
    Boolean update(UpdateUserRequest userUpdateRequest, User loginUser);

}
