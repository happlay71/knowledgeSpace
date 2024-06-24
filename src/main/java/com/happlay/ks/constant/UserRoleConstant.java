package com.happlay.ks.constant;

public class UserRoleConstant {
    // 超级管理员
    public static final String ROOT = "ROOT";
    // 用户管理员
    public static final String USER_ADMIN = "USER_ADMIN";
    // 普通用户
    public static final String USER = "USER";
    // 访客
    public static final String GUEST = "GUEST";

    public static boolean canManageNotes(String role){
        return role != null && (role.equals(ROOT) || role.equals(USER_ADMIN));
    }

    public static boolean canManageAllNotes(String role){
        return role != null && role.equals(ROOT);
    }

    public static boolean canManageOwnNotes(String role){
        return role != null && (role.equals(ROOT) || role.equals(USER_ADMIN) || role.equals(USER));
    }

    public static boolean canNotManageNotes(String role) {
        return role != null && role.equals(GUEST);
    }

    public static boolean isValidRole(String role){
        return role != null && (role.equals(ROOT) || role.equals(USER_ADMIN) || role.equals(USER));
    }
}

