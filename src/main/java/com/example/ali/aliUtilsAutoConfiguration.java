package com.example.ali;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: xgs
 * Description: 测试888
 * @Date: 2022/07/25/17:36
 */
@Configuration
@EnableConfigurationProperties(AliyunResource.class)
public class aliUtilsAutoConfiguration {

    private AliyunResource aliyunResource;

    public aliUtilsAutoConfiguration(AliyunResource aliyunResource) {
        this.aliyunResource = aliyunResource;
    }

    @Bean
    @ConditionalOnMissingBean
    public AliUtils aliUtils(){
        return new AliUtils(aliyunResource);
    }


}
