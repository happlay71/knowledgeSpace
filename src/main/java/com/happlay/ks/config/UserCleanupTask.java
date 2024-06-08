package com.happlay.ks.config;

import com.happlay.ks.service.IUserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserCleanupTask {

    @Resource
    IUserService iUserService;

    // 定时清理
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanDeletedUsers() {
        iUserService.cleanDeletedUsers();
    }
}
