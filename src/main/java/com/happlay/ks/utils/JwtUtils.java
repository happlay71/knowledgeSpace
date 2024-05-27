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
    public static String createToken(Integer userId) {
        SecureDigestAlgorithm<SecretKey, SecretKey> algorithm = Jwts.SIG.HS256;
        long expMillis = System.currentTimeMillis() + 1000L * JwtConstant.EXP;
        Date exp = new Date(expMillis);
        SecretKey key = Keys.hmacShaKeyFor(JwtConstant.KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .signWith(key, algorithm)
                .setExpiration(exp)
                .claim("userId", userId)
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
}
