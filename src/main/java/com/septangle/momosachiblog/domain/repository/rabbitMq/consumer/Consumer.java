package com.septangle.momosachiblog.domain.repository.rabbitMq.consumer;

import com.rabbitmq.client.Channel;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.domain.repository.rabbitMq.producer.Producer;
import com.septangle.momosachiblog.module.rabbit.EmailCheckModule;
import com.septangle.momosachiblog.module.rabbit.EmailSendModule;
import com.septangle.momosachiblog.service.CommentService;
import com.septangle.momosachiblog.service.UserService;
import com.septangle.momosachiblog.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
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

    @RabbitListener(queuesToDeclare = @Queue(name = Producer.EMAIL_SENDER))
    public void emailSenderConsumer(String content, Message message, Channel channel) throws IOException, InterruptedException, MessagingException {
        String []split = content.split(",");
        Long userId = Long.valueOf(split[2]);
        User user = userService.getById(userId);
        EmailSendModule emailSendModule = new EmailSendModule(split[0], split[1]);

        boolean status = false;

        if(Objects.nonNull(user)) {

            //
            if(user.getEmailStatus() == 2) {
                log.error("用户{}邮箱验证不通过", user.getNickname());
                return;
            }
            // 如果该用户的邮箱未验证，则验证邮箱是否存在
            if(user.getEmailStatus() == 1) {
                status = EmailUtils.emailVerify(emailSendModule.getAddress(), "www.septangle.cn");
            }

            if(user.getEmailStatus() == 0) {
                status = true;
                user.setEmailStatus(0);
                userService.updateById(user);
            }


        }else{
             status = EmailUtils.emailVerify(emailSendModule.getAddress(), "www.septangle.cn");
        }

        //验证不通过
        if(!status){
            log.error("邮箱{}验证不通过、发送失败", emailSendModule.getAddress());
            return;
        }

        // 发送邮件
        EmailUtils.sendEmail(emailSendModule.getAddress(), "test", emailSendModule.getContent());

    }
}
