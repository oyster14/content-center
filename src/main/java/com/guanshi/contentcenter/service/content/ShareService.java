package com.guanshi.contentcenter.service.content;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.guanshi.contentcenter.dao.content.ShareMapper;
import com.guanshi.contentcenter.dao.mid_user_share.MidUserShareMapper;
import com.guanshi.contentcenter.dao.rocketmq_transaction_log.RocketmqTransactionLogMapper;
import com.guanshi.contentcenter.domain.dto.content.ShareAuditDTO;
import com.guanshi.contentcenter.domain.dto.content.ShareDTO;
import com.guanshi.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.guanshi.contentcenter.domain.dto.user.UserAddBonusDTO;
import com.guanshi.contentcenter.domain.dto.user.UserDTO;
import com.guanshi.contentcenter.domain.entity.content.Share;
import com.guanshi.contentcenter.domain.entity.mid_user_share.MidUserShare;
import com.guanshi.contentcenter.domain.entity.rocketmq_transaction_log.RocketmqTransactionLog;
import com.guanshi.contentcenter.domain.enums.AuditStatusEnum;
import com.guanshi.contentcenter.feignclient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final Source source;

    private final MidUserShareMapper midUserShareMapper;

    public ShareDTO findById(Integer id) {
        Share share = this.shareMapper.selectByPrimaryKey(id);
        Integer userId = share.getUserId();

//        //get all info of all instances of user-center
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
////        2. 审核资源，将状态设置为PASS/REJECT
//        share.setAuditStatus(auditDTO.getAuditStatusEnum().toString());
//        this.shareMapper.updateByPrimaryKeySelective(share);
////        3.0 如果是PASS添加积分
////        3.1 如果是PASS，发送消息给rocketmq，让用户中心消费，并添加积分
////        同步执行
////      UserDTO userDTO = this.userCenterFeignClient.addBonus(id, 500);
//        this.rocketMQTemplate.convertAndSend(
//                "add-bonus",
//                UserAddBonusMsgDTO.builder()
//                .userId(share.getUserId())
//                .bonus(50)
//                .build()
//        );
//
        if (AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum())) {
//                  发送半消息
            String transactionId = UUID.randomUUID().toString();

            this.source.output()
                    .send(MessageBuilder
                            .withPayload(
                                    UserAddBonusMsgDTO.builder()
                                    .userId(share.getUserId())
                                    .bonus(50)
                                    .build()
                            )
                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                            .setHeader("share_id", id)
                            .setHeader("dto", JSON.toJSONString(auditDTO))
                            .build()
                    );

//            this.rocketMQTemplate.sendMessageInTransaction(
//                    "tx-add-bonus-group",
//                    "add-bonus",
//                    MessageBuilder
//                            .withPayload(
//                                    UserAddBonusMsgDTO.builder()
//                                    .userId(share.getUserId())
//                                    .bonus(50)
//                                    .build()
//                            )
//                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
//                            .setHeader("share_id", id)
//                            .build(),
//                    auditDTO
//            );
        }
        else {
            this.auditByIdInDB(id, auditDTO);
        }
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
    public void auditByIdWithRocketMQLog(Integer id, ShareAuditDTO auditDTO, String transactionId) {
        this.auditByIdInDB(id, auditDTO);

        this.rocketmqTransactionLogMapper.insertSelective(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .log("审核分享。。。。。")
                        .build()
        );
    }

    public PageInfo<Share> q(String title, Integer pageNo, Integer pageSize, Integer userId) {

        PageHelper.startPage(pageNo, pageSize);
        List<Share> shares = this.shareMapper.selectByParam(title);
        List<Share> dealtShares = new ArrayList<>();
//        如果用户未登录，那么downloadURL都为null
        if (userId == null) {
            dealtShares = shares.stream().peek(share -> {
                share.setDownloadUrl(null);
            }).collect(Collectors.toList());
        }
//        如果登陆了，显示mid_user_share有的
        else {
            dealtShares = shares.stream().peek(share -> {
                MidUserShare midUserShare = this.midUserShareMapper.selectOne(
                        MidUserShare.builder()
                                .userId(userId)
                                .shareId(share.getId())
                                .build()
                );
                if (midUserShare == null) {
                    share.setDownloadUrl(null);
                }
            }).collect(Collectors.toList());
        }

        return new PageInfo<>(dealtShares);
    }

    public Share exchangeById(Integer id, HttpServletRequest request) {
//        根据id查询业务，校验是否存在
        Share share = this.shareMapper.selectByPrimaryKey(id);
        Integer price = share.getPrice();
        Object userId = request.getAttribute("id");
        Integer integerUserId = (Integer) userId;
        if (share == null) {
            throw new IllegalArgumentException("该分享不存在！");
        }

//        如果兑换过该分享，直接返回
        MidUserShare midUserShare = this.midUserShareMapper.selectOne(
                MidUserShare.builder()
                        .userId(integerUserId)
                        .shareId(id)
                        .build()
        );
        if (midUserShare != null) {
            return share;
        }

//        根据当前id查询积分是不是够
        UserDTO userDTO = this.userCenterFeignClient.findById(integerUserId);

        if (price > userDTO.getBonus()) {
            throw new IllegalArgumentException("用户积分不够用");
        }
//        扣减积分，并且向mid_user_share插入数据
        this.userCenterFeignClient.addBonus(
                UserAddBonusDTO.builder()
                        .userId(integerUserId)
                        .bonus(-price)
                        .build()
        );
        this.midUserShareMapper.insert(
                MidUserShare.builder()
                        .userId(integerUserId)
                        .shareId(id)
                        .build()
        );
        return share;
    }
}
