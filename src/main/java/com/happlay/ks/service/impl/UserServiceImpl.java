package com.happlay.ks.service.impl;

import com.happlay.ks.model.entity.User;
import com.happlay.ks.mapper.UserMapper;
import com.happlay.ks.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
