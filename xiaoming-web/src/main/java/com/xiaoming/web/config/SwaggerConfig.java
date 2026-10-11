package com.xiaoming.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 接口文档配置（Knife4j 基于 Springfox 3）。
 *
 * 作用：告诉 springfox 「去哪儿找接口、文档长什么样」。
 * 全项目原先没有这个类，所以即使把 knife4j 依赖换成带 UI 的版本，
 * /doc.html 打开了也看不到任何接口 —— 因为没有任何 DocumentationPlugin。
 *
 * 访问地址：http://localhost:8080/api/doc.html （注意 context-path 是 /api）
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xiaoming"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("小明社区平台 接口文档")
                .description("内容社区分享平台 —— 用户 / 笔记 / 分类 / 互动 / 搜索 等接口")
                .version("1.0.0")
                .build();
    }
}
