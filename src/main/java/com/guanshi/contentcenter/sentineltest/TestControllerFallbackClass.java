package com.guanshi.contentcenter.sentineltest;

public class TestControllerFallbackClass {
    public static String fallback(String a) {
//        log.warn("参数非法 fallback：", e);
        return "参数非法 fallback";
    }
}
