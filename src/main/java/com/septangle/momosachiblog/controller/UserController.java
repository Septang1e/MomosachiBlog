package com.septangle.momosachiblog.controller;


import com.septangle.momosachiblog.constant.UserConstant;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.UserLoginDTO;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.domain.repository.rabbitMq.producer.Producer;
import com.septangle.momosachiblog.domain.security.UserClaim;
import com.septangle.momosachiblog.service.UserService;
import com.septangle.momosachiblog.utils.security.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private Producer producer;


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

        if(user == null || username.equals(UserConstant.FAKE_USER_USERNAME) || password.equals(UserConstant.FAKE_USER_PASSWORD)) {
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

    @GetMapping("/api/admin/user/pagination/{current}/{size}")
    public R<String> queryUserPage(@PathVariable int current, @PathVariable int size) {
        return null;
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

    @PostMapping("/api/admin/verify/email")
    public R<String> emailCheck(@RequestParam String email, @RequestParam Long userId) {
        producer.emailCheckerProducer(email, userId);
        return R.success("邮箱验证请求添加成功，请等待验证完成");
    }

}