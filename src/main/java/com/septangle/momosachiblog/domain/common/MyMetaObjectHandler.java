package com.septangle.momosachiblog.domain.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.septangle.momosachiblog.constant.UserConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        //更新 时间的公共字段
        metaObject.setValue("updateTime",new Date());
        metaObject.setValue("createTime",new Date());

        //log.info("currentThreadOnMetaObjectInsert = {}", Thread.currentThread().getName());
        //获取当前线程的UserId
        Long userId=BaseContext.getCurrentId();
        if(userId == null)userId = UserConstant.withoutCreateUser_ID;

        metaObject.setValue("createUser",userId);
        metaObject.setValue("updateUser",userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {

        Long userId = BaseContext.getCurrentId();
        //log.info("currentThreadOnMetaObjectUpdate = {}, userId = {}", Thread.currentThread().getName(), userId);

        metaObject.setValue("updateTime",new Date());
        if(userId == null)userId = UserConstant.withoutCreateUser_ID;
        metaObject.setValue("updateUser", userId);
    }
}
