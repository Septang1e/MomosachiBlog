package com.septangle.momosachiblog.domain.repository.rabbitMq.producer;

import com.septangle.momosachiblog.module.rabbit.EmailCheckModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Producer {
    private final RabbitTemplate rabbitTemplate;

    public final static String EMAIL_CHECKER = "email.checker.#";
    public final static String EMAIL_SENDER = "email.sender.#";

    @Autowired
    public Producer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void emailCheckerProducer(String email, Long userId) {

        String msg = email + "," + userId;
        rabbitTemplate.convertAndSend(EMAIL_CHECKER, msg);
    }

    public void emailSenderProducer(String addr, String content, Long userId) {
        String msg = addr + "," + content + "," + userId;
        rabbitTemplate.convertAndSend(msg, EMAIL_SENDER);
    }


}
