package com.guanshi.contentcenter.configuration;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class UserCenterFeignConfiguration {
    @Bean
    public Logger.Level level() {
//      ask feign to print all requests' details
        return Logger.Level.FULL;
    }
}
