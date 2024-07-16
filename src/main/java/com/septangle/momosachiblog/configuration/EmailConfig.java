package com.septangle.momosachiblog.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class EmailConfig {
    @Bean
    public JavaMailSenderImpl javaMailSenderImpl(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        //smtp服务器
        javaMailSender.setHost("smtp.qq.com");
        //smtp用户名
        javaMailSender.setUsername("2934833295@qq.com");
        //授权码
        javaMailSender.setPassword("xzvjnzbjlshudgid");
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.ssl.enable","true");
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }
}
