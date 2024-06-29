package com.septangle.momosachiblog.domain.repository.redis;

import lombok.Value;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component
public class RateLimiter {


    private RedisTemplate<Object, Object> redisTemplate;

    public RateLimiter(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }



    private String getCombinedKey(JoinPoint point) {

        StringBuilder key = new StringBuilder("rate-limit:");

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        key.append(request.getRemoteAddr());

        // 获取 类和方法
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Class<?> targetClass = method.getDeclaringClass();
        // keyPrefix + "-" + class + "-" + method
        return key.append("-").append( targetClass.getName() )
                .append("-").append(method.getName()).toString();

    }


}
