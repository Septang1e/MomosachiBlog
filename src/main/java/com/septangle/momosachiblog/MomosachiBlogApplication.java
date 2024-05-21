package com.septangle.momosachiblog;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@MapperScan("com.septangle.momosachiblog.mapper")
@Slf4j
@EnableDiscoveryClient
public class MomosachiBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomosachiBlogApplication.class, args);
    }

}