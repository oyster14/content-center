package com.guanshi.contentcenter.configuration;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class GlobalFeignConfiguration {
//    加了@Component注解，否则必须挪到@ComponentScan能扫描到的包以外
    @Bean
    public Logger.Level level() {
//      ask feign to print all requests' details
        return Logger.Level.FULL;
    }
}
