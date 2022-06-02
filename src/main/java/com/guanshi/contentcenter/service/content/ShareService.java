package com.guanshi.contentcenter.service.content;

import com.guanshi.contentcenter.controller.content.dao.content.ShareMapper;
import com.guanshi.contentcenter.controller.content.dao.rocketmq_transaction_log.RocketmqTransactionLogMapper;
import com.guanshi.contentcenter.domain.dto.content.ShareAuditDTO;
import com.guanshi.contentcenter.domain.dto.content.ShareDTO;
import com.guanshi.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.guanshi.contentcenter.domain.dto.user.UserDTO;
import com.guanshi.contentcenter.domain.entity.content.Share;
import com.guanshi.contentcenter.domain.entity.rocketmq_transaction_log.RocketmqTransactionLog;
import com.guanshi.contentcenter.domain.enums.AuditStatusEnum;
import com.guanshi.contentcenter.feignclient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;

    private final UserCenterFeignClient userCenterFeignClient;
//    private final RestTemplate restTemplate;
//    private final DiscoveryClient discoveryClient;
    private final RocketMQTemplate rocketMQTemplate;

    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

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

        if (AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum())) {
//                  发送半消息
            String transactionId = UUID.randomUUID().toString();

            this.rocketMQTemplate.sendMessageInTransaction(
                    "tx-add-bonus-group",
                    "add-bonus",
                    MessageBuilder
                            .withPayload(
                                    UserAddBonusMsgDTO.builder()
                                    .userId(share.getUserId())
                                    .bonus(50)
                                    .build()
                            )
                            .setHeaders(RocketMQHeaders.TRANSACTION_ID, transactionId)
                            .setHeaders("share_id", id)
                            .build(),
                    auditDTO
            );
        }
        else {
            this.auditById(id, auditDTO);
        }
//        2. 审核资源，将状态设置为PASS/REJECT
//        3.0 如果是PASS添加积分
//        同步执行
//        UserDTO userDTO = this.userCenterFeignClient.addBonus(id, 500);
//        3.1 如果是PASS，发送消息给rocketmq，让用户中心消费，并添加积分
//        this.rocketMQTemplate.convertAndSend(
//                "add-bonus",
//                UserAddBonusMsgDTO.builder()
//                .userId(share.getUserId())
//                .bonus(50)
//                .build()
//        );
        return share;
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInDB(Integer id, ShareAuditDTO auditDTO) {
        Share share = Share.builder()
                .id(id)
                .auditStatus(auditDTO.getAuditStatusEnum().toString())
                .reason(auditDTO.getReason())
                .build();
        this.shareMapper.updateByPrimaryKeySelective(share);
//        4 把share写到缓存
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInWithRocketMQLog(Integer id, ShareAuditDTO auditDTO, String transactionId) {
        this.auditById(id, auditDTO);

        this.rocketmqTransactionLogMapper.insertSelective(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .log("审核分享")
                        .build()
        );
    }
}
