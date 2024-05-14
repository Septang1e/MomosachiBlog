package com.septangle.momosachiblog.filter;

import com.alibaba.druid.support.json.JSONUtils;
import com.septangle.momosachiblog.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse)servletResponse;
        HttpServletRequest request = (HttpServletRequest)servletRequest;

        String requestURI = request.getRequestURI();
        log.info("requestURI = {}",requestURI);

        String []urls = new String[]{
                "/api/admin/*",
        };
        boolean check = check(urls,requestURI);
        if(!check){
            filterChain.doFilter(request, response);
            return;
        }
        log.info("登录");
        if(request.getSession().getAttribute("user") != null) {
            filterChain.doFilter(request, response);
            return;
        }

        response.getWriter().write(JSONUtils.toJSONString(R.error("NOT LOGIN")));
        log.info("拦截到请求：{}",requestURI);
    }
    private boolean check(String[] urls, String requestURI){
        for(String url : urls){
            if(PATH_MATCHER.match(requestURI, url)){
                return true;
            }
        }
        return false;
    }
}
