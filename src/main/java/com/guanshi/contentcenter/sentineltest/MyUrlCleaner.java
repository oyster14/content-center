package com.guanshi.contentcenter.sentineltest;


import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
public class MyUrlCleaner implements UrlCleaner {

    @Override
    public String clean(String s) {
        log.info("原始的url是：{}", s);
        return s;
    }
}
