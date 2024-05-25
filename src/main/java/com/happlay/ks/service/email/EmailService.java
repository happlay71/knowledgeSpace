package com.happlay.ks.service.email;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
@Data
public class EmailService{

    @Value("${spring.mail.username}")
    private String senderEmail; // 从配置文件中注入发送人邮箱

    @Resource
    private JavaMailSender mailSender;

    public void sendVerificationCode(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);  // 直接设置完整的发件人邮箱地址
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}


