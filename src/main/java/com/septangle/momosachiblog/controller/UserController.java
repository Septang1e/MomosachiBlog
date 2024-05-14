package com.septangle.momosachiblog.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.septangle.momosachiblog.constant.UserConstant;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.UserLoginDTO;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.domain.security.UserClaim;
import com.septangle.momosachiblog.service.UserService;
import com.septangle.momosachiblog.utils.security.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.parser.Token;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private TokenUtils tokenUtils;


    // 同一个IP 2秒最多10次，目前的想法是 使用redis来维护一个key，key的expire_time为 2s，每次在过期 前使用将会刷新过期 时间 至 初始值
    // AND 如果 2秒 的 申请次数超过10次， 该IP将会被封禁10分钟
    // AND 通过一个key来维护24小时内 超过 申请次数 的次数，若超过3次，该IP将会被封禁1d
    @PostMapping("/api/v1/login")
    public R<String> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest req) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        User user = userService.getUserByUsernameOrNickname(username, username);

        String ip = req.getRemoteAddr();

        log.info("ip为{}的用户正在请求登录", ip);

        if(user == null || username.equals(UserConstant.noneUsername) || password.equals(UserConstant.nonePassword)) {
            return R.error("账号或密码错误");
        }

        //检验 密码 是否正确
        if(!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
            log.info("密码为：{}", DigestUtils.md5DigestAsHex(password.getBytes()));
            return R.error("账号或密码错误");
        }
        String authorization = tokenUtils.getToken(user.getId(), "admin");
        req.getSession().setAttribute("userId", user.getId());

        return R.success(authorization, "登录成功");
    }

    @GetMapping("/api/v1/authorize")
    public R<String> authorizationCheck(HttpServletRequest request) {
        //获取token
        String authorization = request.getHeader("Authorization");
        UserClaim userClaim = tokenUtils.parseToken(authorization);
        if(userClaim == null || !userClaim.getUserRole().equals("admin")) {
            return R.userNotLogin("User-Not-Login");
        }
        return R.success("认证成功");
    }

    @PostMapping("/api/v1/logout")
    public R<String> logout(HttpServletRequest req) {
        req.getSession().removeAttribute("userId");
        return R.success("成功");
    }
}