package com.septangle.momosachiblog.service.Impl;

import com.septangle.momosachiblog.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
public class RabbitServiceImpl implements RabbitService {

    @RabbitListener(queues = "testQueue")
    @Override
    public void consumer(String msg) {
        log.info("rbmq msg is {}", msg);
    }
}
