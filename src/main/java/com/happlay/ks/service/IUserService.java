package com.happlay.ks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.happlay.ks.common.PageRequest;
import com.happlay.ks.model.dto.user.*;
import com.happlay.ks.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.happlay.ks.model.vo.user.AvatarUploadVo;
import com.happlay.ks.model.vo.user.LoginUserVo;
import com.happlay.ks.model.vo.user.UserDetailsVo;
import com.happlay.ks.model.vo.user.UserVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
public interface IUserService extends IService<User> {

    void cleanDeletedUsers();

    LoginUserVo register(RegisterUserRequest request);

    LoginUserVo setUserEmail(String email, User loginUser);

    User getLoginUser(HttpServletRequest request);

    AvatarUploadVo createUploadVo(User avatar);

    AvatarUploadVo uploadAvatar(MultipartFile file, Integer userId);

    Boolean deleteAvatar(User loginUser);

    LoginUserVo createLoginUserVo(User user);

    LoginUserVo login(LoginUserRequest request);

    LoginUserVo adminRegisterUser(AdminRegisterUserRequest request, User loginUser);

    Boolean removeAllById(User user, User loginUser);

    Boolean resetPassword(String email, ResetUserPasswordRequest resetRequest);

    Boolean update(UpdateUserRequest userUpdateRequest, User loginUser);

    UserVo getVo(User user);

    List<UserVo> getVos(List<User> users);

    Page<UserVo> selectPage(PageRequest pageRequest);

    Page<UserVo> selectName(String name, PageRequest pageRequest);

    UserDetailsVo getUserDetailsById(Integer userId, boolean isLoggedIn);

}
