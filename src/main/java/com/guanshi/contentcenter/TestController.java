package com.guanshi.contentcenter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.guanshi.contentcenter.dao.content.ShareMapper;
import com.guanshi.contentcenter.domain.dto.user.UserDTO;
import com.guanshi.contentcenter.domain.entity.content.Share;

import com.guanshi.contentcenter.feignclient.TestBaiduFeignClient;
import com.guanshi.contentcenter.feignclient.TestUserCenterFeignClient;
import com.guanshi.contentcenter.sentineltest.TestControllerBlockerHandlerClass;
import com.guanshi.contentcenter.sentineltest.TestControllerFallbackClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RefreshScope
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

    @Autowired
    private TestUserCenterFeignClient testUserCenterFeignClient;
    @GetMapping("/test-get")
    public UserDTO query(UserDTO userDTO) {
        return this.testUserCenterFeignClient.query(userDTO);
    }

    @Autowired
    private TestBaiduFeignClient testBaiduFeignClient;
    @GetMapping("/baidu")
    public String baiduIndex() {
        return this.testBaiduFeignClient.index();
    }

    @Autowired()
    private TestService testService;
    @GetMapping("/test-a")
    String testA() {
        String common = this.testService.common();
        return "test-a";
    }
    @GetMapping("/test-b")
    String testB() {
        String common = this.testService.common();
        return "test-b";
    }

    @GetMapping("/test-hot")
    @SentinelResource("hot")
    String testHot(
            @RequestParam(required = false) String a,
            @RequestParam(required = false) String b
    ) {
        return a + " " + b;
    }

    @GetMapping("/test-add-flow-rule")
    @SentinelResource("hot")
    String testHot() {
        this.initFlowQpsRule();
        return "success";
    }

    private void initFlowQpsRule() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule("/shares/1");
        // set limit qps to 20
        rule.setCount(20);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    @GetMapping("/test-sentinel-api")
    public String testSentinelAPI(
        @RequestParam(required = false) String a) {
        String resourceName = "test-sentinel-api";

        ContextUtil.enter(resourceName, "test-wfw");

        Entry entry = null;
        try {
            entry = SphU.entry(resourceName);
            if (StringUtils.isEmpty(a)) {
                throw new IllegalArgumentException("a不能为空");
            }
            return a;
        } catch (BlockException e) {
            log.warn("限流了或者降级了：", e);
            return "限流了或者降级了";
        } catch (IllegalArgumentException e2) {
            Tracer.trace(e2);
            return "参数非法";
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    @GetMapping("/test-sentinel-resource")
    @SentinelResource(value = "test-sentinel-api",
            blockHandler = "block",
            blockHandlerClass = TestControllerBlockerHandlerClass.class,
            fallback = "fallback",
            fallbackClass = TestControllerFallbackClass.class
    )
    public String testSentinelResource(
            @RequestParam(required = false) String a) {
        if (StringUtils.isEmpty(a)) {
            throw new IllegalArgumentException("a不能为空");
        }
        return a;
    }

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/test-rest-template-sentinel/{userId}")
    public UserDTO test(@PathVariable Integer userId) {
        return this.restTemplate
                .getForObject(
                        "http://user-center/users/{userId}",
                        UserDTO.class, userId);
    }

    @Autowired
    private Source source;
    @GetMapping("/test-stream")
    public String testStream() {
        this.source.output()
                .send(MessageBuilder
                        .withPayload("消息体")
                        .build());

        return "success";
    }

//    @Autowired
//    private MySource mySource;
//    @GetMapping("/test-stream-2")
//    public String testStream2() {
//        this.mySource.output()
//                .send(MessageBuilder
//                        .withPayload("消息体2")
//                        .build());
//
//        return "success2";
//    }
    @GetMapping("/tokenRelay/{userId}")
    public ResponseEntity<UserDTO>  tokenRelay(@PathVariable Integer userId, HttpServletRequest request) {
        String token = request.getHeader("X-Token");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Token", token);
        return this.restTemplate
                .exchange(
                        "http://user-center/users/{userId}",
                        HttpMethod.GET,
                        new HttpEntity<>(httpHeaders),
                        UserDTO.class,
                        userId
                );
    }

//    @Value("${your.configuration}")
//    private String yourConfiguration;
//
//    @GetMapping("/test-config")
//    public String testConfiguration() {
//        return this.yourConfiguration;
//    }



}
