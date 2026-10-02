package com.xiaoming.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 社区平台启动类
 * 注意：启动类位于 com.xiaoming.web 包，默认只会扫描该包及其子包，
 *      所以必须显式指定扫描 com.xiaoming，才能把各业务模块的 Bean 装配进来。
 */
@SpringBootApplication
@ComponentScan("com.xiaoming")
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
