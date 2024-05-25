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

@Service
public class VerificationService {

    private final ConcurrentHashMap<String, String> emailCodeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> emailCodeTimeMap = new ConcurrentHashMap<>();

    @Resource
    private EmailService emailService;
    @Resource
    IUserService iUserService;

    public void sendVerificationEmail(String email) {
        String code = generateVerificationCode();
        emailCodeMap.put(email, code);
        emailCodeTimeMap.put(email, System.currentTimeMillis());

        String subject = "邮箱验证";
        String text = "您的验证码是: " + code;
        emailService.sendVerificationCode(email, subject, text);
    }

    public void verifyCode(String email, String code) {
        if (!emailCodeMap.containsKey(email)) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "邮箱未找到");
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
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(999999);
        return String.format("%06d", code);
    }
}
