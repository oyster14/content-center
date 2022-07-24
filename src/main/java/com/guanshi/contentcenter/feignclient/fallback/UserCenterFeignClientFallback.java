package com.guanshi.contentcenter.feignclient.fallback;

import com.guanshi.contentcenter.domain.dto.user.UserAddBonusDTO;
import com.guanshi.contentcenter.domain.dto.user.UserDTO;
import com.guanshi.contentcenter.feignclient.UserCenterFeignClient;
import org.springframework.stereotype.Component;

@Component
public class UserCenterFeignClientFallback implements UserCenterFeignClient {

    public UserDTO findById(Integer id) {
        UserDTO userDTO = new UserDTO();
        userDTO.setWxNickname("流控或者降级返回的用户");
        return userDTO;
    }

    @Override
    public UserDTO addBonus(UserAddBonusDTO userAddBonusDTO) {
//        UserDTO userDTO = new UserDTO();
//        userDTO.setWxNickname("流控或者降级返回的用户");
        return null;
    }
}
