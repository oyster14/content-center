package com.guanshi.contentcenter.feignclient;

import com.guanshi.contentcenter.domain.dto.user.UserDTO;
import com.guanshi.contentcenter.feignclient.fallback.UserCenterFeignClientFallback;
import com.guanshi.contentcenter.feignclient.fallbackfactory.UserCenterFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//@FeignClient(name = "user-center", configuration = UserCenterFeignConfiguration.class)
@FeignClient(name = "user-center",
        fallback = UserCenterFeignClientFallback.class//,
//        fallbackFactory = UserCenterFeignClientFallbackFactory.class
)
public interface UserCenterFeignClient {
    @GetMapping("/users/{id}")
    UserDTO findById(@PathVariable Integer id);
    @PostMapping("/users/{id}")
    UserDTO addBonus(@PathVariable Integer id, @RequestParam("bonus") Integer bonus);
}
