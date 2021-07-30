package com.shaoteemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author Smart-T
 *
 * Swagger配置文件修改方式。
 *
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket getDocket(){

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                //指定扫描的包(apis)
                .apis(RequestHandlerSelectors.basePackage("com.shaoteemo.controller"))
                .build();
    }

    private ApiInfo getApiInfo(){
        return new ApiInfoBuilder().title("Activity接口测试")
                .description("Activity基本案例")
                .version("v1.0")
                .contact(new Contact("ShaoTeemo","http://git.shaoteemo.com","shaoteemo@qq.com"))
                .build();
    }
}