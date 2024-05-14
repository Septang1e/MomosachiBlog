package com.septangle.momosachiblog.utils.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.septangle.momosachiblog.constant.Constants;
import com.septangle.momosachiblog.domain.security.UserClaim;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class TokenUtils {

    private final String secretKey = Constants.getAuthorizationKey("onChecking_wacjweRkz");

    //加密
    public String getToken(Long userId, String userRole) {
        return JWT
                .create()
                .withClaim("userId", userId)
                .withClaim("userRole", userRole)
                .withClaim("timestamp", System.currentTimeMillis())
                .sign(Algorithm.HMAC256(secretKey));
    }

    //解密
    public UserClaim parseToken(String token) {

        if(token == null || token.isBlank() || token.isEmpty()) {
            return null;
        }

        try{
            UserClaim userClaim = new UserClaim();
            //获取JWT中保留的信息
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKey))
                    .build().verify(token);
            Long timestamp = decodedJWT.getClaim("timestamp").asLong();
            Long userId = decodedJWT.getClaim("userId").asLong();
            String userRole = decodedJWT.getClaim("userRole").asString();
            return new UserClaim(userId, userRole, timestamp);
        }catch (JWTDecodeException e) {
            return null;
        }
    }

}
