package com.septangle.momosachiblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.septangle.momosachiblog.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User getUserByUsernameOrNickname(String username, String nickname);
}
