package com.happlay.ks.service.email;

import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.service.IUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VerificationService {

    private final ConcurrentHashMap<String, String> emailCodeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> emailCodeTimeMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Long> lastSentVerificationEmailTimeMap = new ConcurrentHashMap<>();

    @Resource
    private EmailService emailService;

    public void sendVerificationEmail(String email) {
        // 检查邮箱是否为空
        if (email == null || email.trim().isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }

        // 正则表达式验证邮箱格式
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 检查发送频率，例如每5分钟只能发送一次验证码
        if (!canSendVerificationEmail(email)) {
            throw new CommonException(429, "请稍后再试发送验证码");
        }

        String code = generateVerificationCode();
        emailCodeMap.put(email, code);
        emailCodeTimeMap.put(email, System.currentTimeMillis());
        System.out.println("emailCodeTimeMap: " + emailCodeTimeMap);

        String subject = "邮箱验证";
        String text = "您的验证码是: " + code;
        emailService.sendVerificationCode(email, subject, text);

        // 更新上次发送验证码的时间
        updateLastVerificationEmailTime(email);
    }

    public boolean verifyCode(String email, String code) {
        // 打印调试信息
        System.out.println("emailCodeTimeMap: " + emailCodeTimeMap);
        System.out.println("email: " + email);
        if (email == null || code == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "邮箱和验证码不能为空");
        }

        if (!emailCodeMap.containsKey(email)) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "未向该邮箱发送验证码");
        }

        if (!emailCodeTimeMap.containsKey(email)) {
            throw new CommonException(ErrorCode.VERIFICATION_CODE_ERROR, "验证码发送时间未找到");
        }

        long currentTime = System.currentTimeMillis();
        long sentTime = emailCodeTimeMap.get(email);
        if (currentTime - sentTime > TimeUnit.MINUTES.toMillis(5)) {
            emailCodeMap.remove(email);
            emailCodeTimeMap.remove(email);
            throw new CommonException(ErrorCode.VERIFICATION_CODE_ERROR, "验证码失效");
        }

        if (!emailCodeMap.get(email).equals(code)) {
            throw new CommonException(ErrorCode.VERIFICATION_CODE_ERROR, "验证码不正确");
        }
        // 验证成功，执行相应操作
        // TODO: 执行验证通过后的操作

        // 验证成功后移除对应的记录
        emailCodeMap.remove(email);
        emailCodeTimeMap.remove(email);

        return true;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(999999);
        return String.format("%06d", code);
    }

    private boolean canSendVerificationEmail(String email) {
        Long lastSentTime = lastSentVerificationEmailTimeMap.get(email);
        if (lastSentTime == null) {
            return true; // 第一次发送，允许发送
        }

        long currentTime = System.currentTimeMillis();
        return currentTime - lastSentTime > 5 * 60 * 1000; // 5分钟
    }

    private void updateLastVerificationEmailTime(String email) {
        lastSentVerificationEmailTimeMap.put(email, System.currentTimeMillis());
    }
}
