package com.septangle.momosachiblog.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.septangle.momosachiblog.constant.UserConstant;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.UserLoginDTO;
import com.septangle.momosachiblog.domain.entity.Comment;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.domain.repository.rabbitMq.producer.Producer;
import com.septangle.momosachiblog.domain.security.UserClaim;
import com.septangle.momosachiblog.service.CommentService;
import com.septangle.momosachiblog.service.UserService;
import com.septangle.momosachiblog.utils.security.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

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
    @Autowired
    private CommentService commentService;


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

        if(Objects.isNull(user) || username.equals(UserConstant.FAKE_USER_USERNAME) || password.equals(UserConstant.FAKE_USER_PASSWORD)) {
            return R.error("账号或密码错误");
        }

        //检验 密码 是否正确
        if(!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
            log.info("密码为：{}", DigestUtils.md5DigestAsHex(password.getBytes()));
            return R.error("账号或密码错误");
        }
        String authorization = tokenUtils.getToken(user.getId(), user.getIsAdmin() == 1 ? "admin" : "user");
        req.getSession().setAttribute("userId", user.getId());

        return R.success(authorization, "登录成功");
    }
    @PostMapping("/api/v1/register")
    public R<String> register(@RequestBody UserLoginDTO userDTO) {

        return R.success("注册成功");
    }

    /**
     *
     * @param current 当前页数
     * @param size 每页数量
     * @param order 排序方式
     * @param status 是否为管理员
     * @param is_deleted 是否被删除
     * @param email_status 邮箱状态
     * @return 分页结果
     */
    @GetMapping("/api/admin/user/pagination/{current}/{size}")
    public R<Page<User>> queryUserPage(@PathVariable int current, @PathVariable int size,
                                       @RequestParam(required = false, value = "create-time") String order,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Integer is_deleted,
                                       @RequestParam(required = false) Integer email_status
    ) {

        return R.success(pagination(current, size, order, is_deleted, status, email_status));
    }

    @DeleteMapping("/api/admin/user")
    public R<String> deleteUser(@RequestBody Long []userList) {
        for(Long id : userList) {
            User user = userService.getById(id);
            if(Objects.isNull(user)) {
                return R.error("id为 " + id + " 用户不存在");
            }
            user.setIsDelete(1);
            userService.updateById(user);
        }
        return R.success("用户删除成功，数据可在回收站中恢复");
    }
    @DeleteMapping("/api/admin/bin/user")
    public R<String> removeFromDatabase(@RequestBody Long []userList) {
        for(Long id : userList) {
            User user = userService.getById(id);
            if(Objects.isNull(user)) {
                log.error("id为{}的用户不存在", id);
                return R.error("id为 " + id + " 用户不存在");
            }
            LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLambdaQueryWrapper
                    .eq(Comment::getReplyBy, id);

            // 删除与当前用户相关评论
            commentService.remove(commentLambdaQueryWrapper);
            userService.removeById(user);
        }

        return R.success("用户删除成功");
    }
    @PostMapping("/api/admin/user")
    public R<String> update(@RequestBody User user) {

        if(Objects.isNull(user.getId())) {

        }



        return R.success("用户数据更新成功");
    }

    @PostMapping("/api/admin/bin/user")
    public R<String> recycle(@RequestBody Long []userList) {
        for(Long id : userList) {
            User user = userService.getById(id);
            if(Objects.isNull(user)) {
                log.error("id为{}的用户不存在", id);
                return R.error("id为 " + id + " 用户不存在");
            }
            user.setIsDelete(0);
            userService.updateById(user);
        }
        return R.success("数据恢复成功");
    }
    @GetMapping("/api/admin/user/count")
    public R<Long> getUserCount() {

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getIsDelete, 0);

        Long count = userService.count(userLambdaQueryWrapper);

        return R.success(count);
    }

    private Page<User> pagination(int current, int size, String order,
                                  Integer isDeleted, Integer status, Integer emailStatus) {
        Page<User> page = new Page<>(current, size);

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(Objects.nonNull(isDeleted)) {
            userLambdaQueryWrapper
                    .eq(User::getIsDelete, isDeleted);
        }
        if(Objects.nonNull(status)) {
            userLambdaQueryWrapper
                    .eq(User::getIsAdmin, status);
        }
        if(Objects.nonNull(emailStatus)) {
            userLambdaQueryWrapper
                    .eq(User::getEmailStatus, emailStatus);
        }
        if("create-time".equals(order)) {
            userLambdaQueryWrapper
                    .orderByAsc(User::getCreateTime);
        }

        userService.page(page, userLambdaQueryWrapper);

        // 将Fake_User的username和password设为特殊标识符
        List<User> record = page.getRecords().stream().peek((user)->{
            if(user.getUsername().equals(UserConstant.FAKE_USER_USERNAME)) {
                user.setUsername("Fake_User");
                user.setPassword("...");
            }
        }).toList();
        page.setRecords(record);
        return page;
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