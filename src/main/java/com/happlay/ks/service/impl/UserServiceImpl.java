package com.happlay.ks.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.common.PageRequest;
import com.happlay.ks.constant.UserRoleConstant;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.dto.folder.CreateFolderRequest;
import com.happlay.ks.model.dto.user.*;
import com.happlay.ks.model.entity.Folder;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.mapper.UserMapper;
import com.happlay.ks.model.vo.folder.FolderDetailsVo;
import com.happlay.ks.model.vo.user.AvatarUploadVo;
import com.happlay.ks.model.vo.user.LoginUserVo;
import com.happlay.ks.model.vo.user.UserDetailsVo;
import com.happlay.ks.model.vo.user.UserVo;
import com.happlay.ks.service.IFileService;
import com.happlay.ks.service.IFolderService;
import com.happlay.ks.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.utils.file.FileUtils;
import com.happlay.ks.utils.JwtUtils;
import com.happlay.ks.utils.folder.FolderUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Value("${file.storage.root.path}")
    private String storageRootPath;

    @Resource
    FileUtils fileUtils;

    @Resource
    FolderUtils folderUtils;

    @Resource
    IFolderService iFolderService;

    @Resource
    IFileService iFileService;

    @Resource
    UserMapper userMapper;

    @Override
    public void cleanDeletedUsers() {
        // 查找isDelete为1的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDelete, 1);
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            userMapper.deleteById(user.getId());
        }
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        String role = JwtUtils.getUserRoleFromToken(request.getHeader("token"));
        if (role.equals(UserRoleConstant.GUEST)) {
            User user = new User();
            BeanUtil.copyProperties(createGuestUserVo(), user);
            return user;
        }
        return this.getById(JwtUtils.getUserIdFromToken(request.getHeader("token")));
    }

    @Override
    public LoginUserVo createLoginUserVo(User user) {
        System.out.println(user);
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtil.copyProperties(user, loginUserVo);
        loginUserVo.setToken(JwtUtils.createUserToken(user.getId(), user.getRole()));
        return loginUserVo;
    }

    private LoginUserVo createGuestUserVo() {
        LoginUserVo guestUserVo = new LoginUserVo();
        guestUserVo.setId(null);  // 访客没有ID
        guestUserVo.setUsername("Guest");
        guestUserVo.setRole(UserRoleConstant.GUEST);  // 访客角色设为 GUEST
        guestUserVo.setToken(JwtUtils.createUserToken(guestUserVo.getId(), guestUserVo.getRole()));  // 访客token
        return guestUserVo;
    }

    @Override
    public LoginUserVo login(LoginUserRequest request) {
        String guest = UserRoleConstant.GUEST;
        if (Objects.equals(request.getUsername(), guest) && Objects.equals(request.getPassword(), "654321")) {
            // 如果没有提供用户名或密码，则生成访客信息
            return createGuestUserVo();
        }

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
        if (username == null || username.trim().isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty() || request.getPassword2() == null || request.getPassword2().trim().isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
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

        // 创建根目录
        CreateFolderRequest createFolderRequest = new CreateFolderRequest(0, username);
        iFolderService.createFolder(createFolderRequest, user, false);

        return createLoginUserVo(user);
    }

    @Override
    public LoginUserVo setUserEmail(String email, User loginUser) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (this.getOne(wrapper) != null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "邮箱已被注册");
        }

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
    public AvatarUploadVo createUploadVo(User avatar) {
        System.out.println(avatar);
        AvatarUploadVo avatarUploadVo = new AvatarUploadVo();
        BeanUtil.copyProperties(avatar, avatarUploadVo);
        return avatarUploadVo;
    }

    @Override
    public AvatarUploadVo uploadAvatar(MultipartFile file, Integer userId) {
        // 查询当前用户是否已有头像记录
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User oldAvatar = this.getOne(queryWrapper);

        if (oldAvatar.getAvatarUrl() != null) {
            // 删除路径中保存的头像图片及用户 ID 文件夹
            String basePath = new File(storageRootPath).getAbsolutePath();
            System.out.println(basePath);
            String relativePath = oldAvatar.getAvatarUrl().replaceFirst("^ksStatic\\\\", "");
            Path userDir = Paths.get(basePath, relativePath).getParent();
            System.out.println(userDir);
            if (Files.exists(userDir)) {
                try {
                    Files.walk(userDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new CommonException(ErrorCode.SYSTEM_ERROR, "删除旧头像文件夹失败");
                }
            }
        }

        // 更新数据库中当前头像路径信息
        String avatarUrl = fileUtils.saveFile(file, FileTypeEnum.AVATAR, userId);
        oldAvatar.setAvatarUrl(avatarUrl);
        oldAvatar.setUpdateUser(userId);
        this.updateById(oldAvatar);
        return createUploadVo(oldAvatar);
    }

    @Override
    public Boolean deleteAvatar(User loginUser) {
        String avatarUrl = loginUser.getAvatarUrl();

        if (avatarUrl != null) {
            // 删除头像文件
            fileUtils.deleteFileFromPath(avatarUrl);

            // 删除头像文件夹
            folderUtils.deleteFolderById(FileTypeEnum.AVATAR, loginUser.getId());

            // 更新用户头像URL为null
            loginUser.setAvatarUrl(null);
        }

        return true;
    }

    private String getStorageRootPath() {
        //获取jar包所在目录
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        return Paths.get(jarF.getParentFile().getParent(), "ksStatic").toString();
    }

    @Override
    public LoginUserVo adminRegisterUser(AdminRegisterUserRequest request, User loginUser) {
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty() || request.getPassword2() == null || request.getPassword2().trim().isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
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
        // 创建根目录
        CreateFolderRequest createFolderRequest = new CreateFolderRequest(0, user.getUsername());
        iFolderService.createFolder(createFolderRequest, user, false);
        return createLoginUserVo(user);
    }

    @Override
    public Boolean removeAllById(User user, User loginUser) {
        // 被删除用户信息
        User userToDelete = this.getById(user.getId());
        if (userToDelete == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        // 用户权限
        String loginUserRole = loginUser.getRole();
        String userToDeleteRole = userToDelete.getRole();

        // 超级管理员，可以删除本身外的任何用户
        if (loginUserRole.equals(UserRoleConstant.ROOT) && userToDeleteRole.equals(loginUserRole)) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "超级管理员不能删除自己");
        }
        // 普通管理员，不能删除管理员或超级管理员
        if (loginUserRole.equals(UserRoleConstant.USER_ADMIN)) {
            if (userToDeleteRole.equals(UserRoleConstant.USER_ADMIN) || userToDeleteRole.equals(UserRoleConstant.ROOT)) {
                throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "权限不足，不能删除管理员或超级管理员");
            }
        }
        // 普通用户，只能删除自己
        if (loginUserRole.equals(UserRoleConstant.USER)) {
            if (!Objects.equals(loginUser.getId(), user.getId())) {
                throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "不能修改他人信息");
            }
        }

        // 执行删除操作
        // 1.头像删除
        deleteAvatar(user);
        // 2.文件删除
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getUserId, user.getId()).eq(Folder::getParentId, 0);
        Folder folder = iFolderService.getOne(queryWrapper);
        iFolderService.deleteById(folder.getId(), user, false);
        // 3.用户数据删除
        boolean isRemoved = userMapper.deleteById(user.getId());
        if (!isRemoved) {
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "删除用户失败");
        }

        return isRemoved;
    }

    @Override
    public Boolean resetPassword(String email, ResetUserPasswordRequest resetRequest) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        User user = this.getOne(queryWrapper);
        if (user == null) throw new CommonException(ErrorCode.PARAMS_ERROR, "用户不存在");
        if (!resetRequest.getPassword().equals(resetRequest.getPassword2()))
            throw new CommonException(ErrorCode.PARAMS_ERROR, "两次密码不匹配");
        user.setPassword(resetRequest.getPassword());
        user.setUpdateUser(user.getId());

        return this.updateById(user);
    }

    @Override
    public Boolean update(UpdateUserRequest userUpdateRequest, User loginUser) {
        // 找到待修改的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userUpdateRequest.getId());
        User oldUser = this.getOne(queryWrapper);
        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().trim().isEmpty()) {
            oldUser.setUsername(userUpdateRequest.getUsername());
        }
        if (userUpdateRequest.getPassword() != null && !userUpdateRequest.getPassword().trim().isEmpty()) {
            if (!userUpdateRequest.getPassword().equals(userUpdateRequest.getPassword2())) {
                throw new CommonException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
            }
            oldUser.setPassword(userUpdateRequest.getPassword());
        }
        // 如果登录用户为普通用户，则只能修改自身信息
        if (loginUser.getRole().equals(UserRoleConstant.USER)) {
            if (!Objects.equals(loginUser.getId(), userUpdateRequest.getId())) {
                throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "普通用户，权限不足");
            }
        } else if (loginUser.getRole().equals(UserRoleConstant.USER_ADMIN)) {
            // 如果登录用户为管理员，则能修改普通用户信息
            if (!Objects.equals(loginUser.getId(), userUpdateRequest.getId())) {
                if (oldUser.getRole().equals(UserRoleConstant.USER_ADMIN)
                        || oldUser.getRole().equals(UserRoleConstant.ROOT)
                        || userUpdateRequest.getRole().equals(UserRoleConstant.USER_ADMIN)) {
                    throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "权限不足");
                }
            }
        }

        if (Objects.equals(userUpdateRequest.getRole(), UserRoleConstant.ROOT)) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "不允许添加新的超级管理员");
        } else if (userUpdateRequest.getRole() != null && !userUpdateRequest.getRole().trim().isEmpty()) {
            oldUser.setRole(userUpdateRequest.getRole());
        }

        oldUser.setUpdateUser(loginUser.getId());
        return this.updateById(oldUser);
    }

    @Override
    public UserVo getVo(User user) {
        if (user == null) throw new CommonException(ErrorCode.NOT_FOUND_ERROR);
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public List<UserVo> getVos(List<User> users) {
        ArrayList<UserVo> userVos = new ArrayList<>();
        for (User user : users) {
            userVos.add(getVo(user));
        }
        return userVos;
    }

    @Override
    public Page<UserVo> selectPage(PageRequest pageRequest) {
        Page<User> userPage = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        this.page(userPage);
        Page<UserVo> userVoPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        userVoPage.setRecords(this.getVos(userPage.getRecords()));
        return userVoPage;
    }

    @Override
    public Page<UserVo> selectUserName(String name, PageRequest pageRequest) {
        // 模糊查询匹配对象
        Page<User> userPage = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), User::getUsername, name);
        queryWrapper.orderByDesc(User::getUpdateTime);
        this.page(userPage, queryWrapper);

        Page<UserVo> userVoPage = new Page<>();
        BeanUtil.copyProperties(userPage, userVoPage, "records");

        List<User> records = userPage.getRecords();
        List<UserVo> list = records.stream().map((user -> {
            UserVo userVo = new UserVo();
            BeanUtil.copyProperties(user, userVo);
            return userVo;
        })).collect(Collectors.toList());

        userVoPage.setRecords(list);
        return userVoPage;
    }

    public UserDetailsVo getUserDetailsById(Integer userId, boolean isLoggedIn) {
        User user = this.getById(userId);
        if (user == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        UserDetailsVo userDetailsVo = new UserDetailsVo();
        userDetailsVo.setId(user.getId());
        userDetailsVo.setUsername(user.getUsername());
        userDetailsVo.setRole(user.getRole());
        userDetailsVo.setAvatarUrl(user.getAvatarUrl());

        FolderDetailsVo folderDetailsVo = iFolderService.getFolderStructureByUserId(userId);
        iFileService.addFilesToFolders(folderDetailsVo, isLoggedIn);

        userDetailsVo.setFolders(folderDetailsVo);

        return userDetailsVo;
    }

}
