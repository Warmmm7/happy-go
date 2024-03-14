package com.shark;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)//暴露代理对象
@MapperScan("com.shark.mapper")
@SpringBootApplication
public class HappyGoApplication {
    public static void main(String[] args) {
        SpringApplication.run(HappyGoApplication.class,args);
    }
}
