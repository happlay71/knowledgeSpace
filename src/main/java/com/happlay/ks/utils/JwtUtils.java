package com.happlay.ks.utils;

import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.constant.JwtConstant;
import com.happlay.ks.exception.CommonException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {

    private static final long USER_EXP = 60*60*24L;  // 用户token有效期
    private static final long GUEST_EXP = 60*60*3L;  // 访客token有效期 3小时
    private static final long EMAIL_EXP = 600L;  // 用户token有效期

    /**
     * 生成用户、邮箱token
     * Jwts.builder:
     * iss(Issuser)：代表这个JWT的签发主体；
     * sub(Subject)：代表这个JWT的主体，即它的所有人；
     * aud(Audience)：代表这个JWT的接收对象；
     * exp(Expiration time)：是一个时间戳，代表这个JWT的过期时间；
     * nbf(Not Before)：是一个时间戳，代表这个JWT生效的开始时间，意味着在这个时间之前验证JWT是会失败的；
     * iat(Issued at)：是一个时间戳，代表这个JWT的签发时间；
     * jti(JWT ID)：是JWT的唯一标识。
     * @return
     */
    public static String createUserToken(Integer userId, String role) {
        SecureDigestAlgorithm<SecretKey, SecretKey> algorithm = Jwts.SIG.HS256;
        long expMillis = System.currentTimeMillis() + 1000L * USER_EXP;
        Date exp = new Date(expMillis);
        SecretKey key = Keys.hmacShaKeyFor(JwtConstant.KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .signWith(key, algorithm)
                .expiration(exp)
                .claim("userId", userId)
                .claim("role", role)
                .compact();
    }

    public static String createEmailToken(String email) {
        SecureDigestAlgorithm<SecretKey, SecretKey> algorithm = Jwts.SIG.HS256;
        long expMillis = System.currentTimeMillis() + 1000L * EMAIL_EXP;
        Date exp = new Date(expMillis);
        SecretKey key = Keys.hmacShaKeyFor(JwtConstant.KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .signWith(key, algorithm)
                .expiration(exp)
                .claim("email", email)
                .compact();
    }

    public static Integer getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(JwtConstant.KEY.getBytes(StandardCharsets.UTF_8));
        Integer result;
        try {
            // 解析token
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            // 获取返回值
            result = claimsJws.getPayload().get("userId", Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.TOKEN_ERROR);
        }
        return result;
    }

    public static String getUserRoleFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(JwtConstant.KEY.getBytes(StandardCharsets.UTF_8));
        String result;
        try {
            // 解析token
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            // 获取返回值
            result = claimsJws.getPayload().get("role", String.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.TOKEN_ERROR);
        }
        return result;
    }

    public static String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(JwtConstant.KEY.getBytes(StandardCharsets.UTF_8));
        String result;
        try {
            // 解析token
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            // 获取返回值
            result = claimsJws.getPayload().get("email", String.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.TOKEN_ERROR);
        }
        return result;
    }
}
