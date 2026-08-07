package com.xiaoming.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 社区平台启动类
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@MapperScan("com.xiaoming.**.mapper")
public class CommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
        System.out.println("========================================");
        System.out.println("  小明社区平台启动成功！");
        System.out.println("  接口文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
