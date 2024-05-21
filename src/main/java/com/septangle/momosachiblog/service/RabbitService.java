package com.septangle.momosachiblog.service;

import org.springframework.stereotype.Service;


public interface RabbitService {
    public void consumer(String msg);
}
