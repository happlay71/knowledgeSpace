package com.happlay.ks.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.happlay.ks.annotation.LoginCheck;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.service.IUserService;
import com.happlay.ks.utils.JwtUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;

public class RoleInterceptor {
    @Resource
    private IUserService iUserService;

    @Around("@annotation(loginCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, LoginCheck loginCheck) throws Throwable {
        // 获取必须的权限数组,有其中之一即可继续执行
        String[] mustRole = loginCheck.mustRole();
        System.out.println(Arrays.deepToString(mustRole));
        // 获取当前请求的上下文
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes( );
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest( );
        // 获取请求头中的token
        String token = request.getHeader("token");
        // token为空
        if(StringUtils.isBlank(token)){
            throw new CommonException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 解析token
        Integer userId = JwtUtils.getUserIdFromToken(token);
        // 获取当前登录的用户
        User userById = iUserService.getById(userId);
        if(userById == null){
            throw new CommonException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 仅登录
        if(mustRole.length == 0) return joinPoint.proceed();
        // 需要的权限放入set
        HashSet<String> set = new HashSet<>(Arrays.asList(mustRole));
        // 遍历当前具有的权限,当前具有的权限在set中说明可以通过
        if(set.contains(userById.getRole())) return joinPoint.proceed();
        throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "无权限");
    }
}
