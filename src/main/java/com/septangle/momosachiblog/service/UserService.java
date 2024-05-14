package com.septangle.momosachiblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.septangle.momosachiblog.domain.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {
    User getUserByUsernameOrNickname(String username, String nickname);
}
