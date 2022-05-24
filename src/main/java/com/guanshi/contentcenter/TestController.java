package com.guanshi.contentcenter;

import com.guanshi.contentcenter.dao.content.ShareMapper;
import com.guanshi.contentcenter.domain.entity.content.Share;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
public class TestController {
    @Autowired
    private ShareMapper shareMapper;
    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/test")
    public List<Share> testInsert() {

        Share share = new Share();
        share.setCreateTime(new Date());
        share.setUpdateTime(new Date());
        share.setCover("hahahh");
        share.setTitle("asfdasf");
        share.setAuthor("王");
        share.setBuyCount(1);

        this.shareMapper.insertSelective(share);

        List<Share> shares = this.shareMapper.selectAll();
        return shares;
    }

    /**
     * test nacos discovery, and prove content-center can always find user-center
     * @return add info of all instances of user-center
     */
    @GetMapping("/test2")
    public List<ServiceInstance> setDiscoveryClient() {
        return this.discoveryClient.getInstances("user-center");
    }
}
