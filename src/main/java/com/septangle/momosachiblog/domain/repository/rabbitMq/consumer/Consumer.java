package com.septangle.momosachiblog.domain.repository.rabbitMq.consumer;

import com.rabbitmq.client.Channel;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.domain.repository.rabbitMq.producer.Producer;
import com.septangle.momosachiblog.module.rabbit.EmailCheckModule;
import com.septangle.momosachiblog.service.CommentService;
import com.septangle.momosachiblog.service.UserService;
import com.septangle.momosachiblog.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;


@Component
@Slf4j
public class Consumer {

    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;

    @RabbitListener(queuesToDeclare = @Queue(name = Producer.EMAIL_CHECKER))
    public void emailCheckConsumer(String content, Message message, Channel channel) throws IOException, InterruptedException {
        // 通过Message对象解析消息
        EmailCheckModule emailCheckModule = new EmailCheckModule(Long.valueOf(content.split(",")[1]), content.split(",")[0]);
        Long userId = emailCheckModule.getUserId();
        User user = userService.getById(userId);

        if(Objects.isNull(user)) {
            log.info("邮箱验证更新失败！！用户{}不存在", userId);
            return;
        }

        if(EmailUtils.emailVerify(emailCheckModule.getEmail(), "www.septangle.cn")) {
            user.setEmailStatus(0);
        }else{
            log.info("{}验证失败", emailCheckModule.getEmail());
            user.setEmailStatus(2);
        }
        userService.updateById(user);

    }
}
