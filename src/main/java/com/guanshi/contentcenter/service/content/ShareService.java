package com.guanshi.contentcenter.service.content;

import com.guanshi.contentcenter.dao.content.ShareMapper;
import com.guanshi.contentcenter.domain.dto.content.ShareAuditDTO;
import com.guanshi.contentcenter.domain.dto.content.ShareDTO;
import com.guanshi.contentcenter.domain.dto.user.UserDTO;
import com.guanshi.contentcenter.domain.entity.content.Share;
import com.guanshi.contentcenter.feignclient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;

    private final UserCenterFeignClient userCenterFeignClient;
//    private final RestTemplate restTemplate;
//    private final DiscoveryClient discoveryClient;

    public ShareDTO findById(Integer id) {
        Share share = this.shareMapper.selectByPrimaryKey(id);
        Integer userId = share.getUserId();

//        // get all info of all instances of user-center
//        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
//        List<String> targetURLS = instances.stream()
//                .map(instance -> instance.getUri().toString() + "/users/{id}")
//                .collect(Collectors.toList());
//
//        int i = ThreadLocalRandom.current().nextInt(targetURLS.size());
//
//        log.info("requested targetURL = {}", targetURLS.get(i));

//        UserDTO userDTO = this.restTemplate.getForObject(
//                "http://user-center/users/{userId}",
//                UserDTO.class,
//                userId
//        );
        UserDTO userDTO = this.userCenterFeignClient.findById(userId);

        ShareDTO shareDTO = new ShareDTO();

        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());

        return shareDTO;

    }

    public Share auditById(Integer id, ShareAuditDTO auditDTO) {
//        1. 查询share是否存在，不存在或者当前状态!=NOT_YET 抛异常
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if (share == null) {
            throw new IllegalArgumentException("参数非法，这个分享不存在");
        }
        if (!Objects.equals("NOT_YET", share.getAuditStatus())) {
            throw new IllegalArgumentException("参数非法，这个分享已经审核通过或者审核不通过");
        }
//        2. 审核资源，将状态设置为PASS/REJECT
        share.setAuditStatus(auditDTO.getAuditStatusEnum().toString());
        this.shareMapper.updateByPrimaryKey(share);
//        3. 如果是PASS添加积分
//        同步执行
//        userCenterFeignClient.addBonus(id, 500);


    }
}
