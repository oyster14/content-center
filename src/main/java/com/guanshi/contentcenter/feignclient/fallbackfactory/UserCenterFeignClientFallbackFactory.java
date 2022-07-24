package com.guanshi.contentcenter.feignclient.fallbackfactory;

import com.guanshi.contentcenter.domain.dto.user.UserAddBonusDTO;
import com.guanshi.contentcenter.domain.dto.user.UserDTO;
import com.guanshi.contentcenter.feignclient.UserCenterFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserCenterFeignClientFallbackFactory
        implements FallbackFactory<UserCenterFeignClient> {
    @Override
    public UserCenterFeignClient create(Throwable throwable) {
        return new UserCenterFeignClient() {
            @Override
            public UserDTO findById(Integer id) {
                log.warn("远程调用限流或者被降级了", throwable);
                UserDTO userDTO = new UserDTO();
                userDTO.setWxNickname("流控或者降级返回的用户");
                return userDTO;
            }

            @Override
            public UserDTO addBonus(UserAddBonusDTO userAddBonusDTO) {
                log.warn("远程调用限流或者被降级了", throwable);
//                UserDTO userDTO = new UserDTO();
//                userDTO.setWxNickname("流控或者降级返回的用户");
                return null;
            }

        };
    }
}
