package com.septangle.momosachiblog.filter;

import com.septangle.momosachiblog.domain.common.BaseContext;
import com.septangle.momosachiblog.domain.security.UserClaim;
import com.septangle.momosachiblog.utils.security.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthHandlerInterceptor implements HandlerInterceptor {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired
    private TokenUtils tokenUtil;

    @Value("${constant.token.refresh-time}")
    private Long refreshTime;
    @Value("${constant.token.expire-time}")
    private Long expiresTime;
    /**
     * 权限认证的拦截操作.
     */

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        log.info("用户 {} 正在访问网站，正在进行拦截", request.getRemoteAddr());

        //获取 request 的URI
        String requestURI = request.getRequestURI();
        String []urls = new String[]{
                "/api/admin/**"
        };
        //看该URI是否需要被拦截
        boolean check = check(urls, requestURI);
        //log.info("requestURI = {}, check = {}", requestURI, check);
        //log.info("currentThread = {}", Thread.currentThread().getName());
        //该URI不需要被拦截
        if(check) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        UserClaim userClaim = tokenUtil.parseToken(authorization);
        //该URI需要被拦截，或者客户端申请的header中没有token
        if(userClaim == null || !userClaim.getUserRole().equals("admin")) {
            response.getWriter().write("Invalid Token");
            return false;
        }

        BaseContext.setCurrentId(userClaim.getUserId());
        //token过期以后的情况
        long usedTime = System.currentTimeMillis() - userClaim.getTimestamp();
        if(usedTime >= expiresTime) {
            response.getWriter().write("Token Expired");
            return false;
        }

        //log.info("currentThread = {}", Thread.currentThread().getName());

        return true;
    }
    private boolean check(String[] urls, String requestURI){
        for(String url : urls){
            if(PATH_MATCHER.match(url, requestURI)){
                return false;
            }
        }
        return true;
    }


}
