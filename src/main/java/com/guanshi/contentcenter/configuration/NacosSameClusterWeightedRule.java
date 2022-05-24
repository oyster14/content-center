package com.guanshi.contentcenter.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class NacosSameClusterWeightedRule extends AbstractLoadBalancerRule {
    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object o) {

        try {
            String clusterName = nacosDiscoveryProperties.getClusterName();

            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
//          log.info("loadBalancer = {}", loadBalancer);

//          the required name
            String name = loadBalancer.getName();
//          get service discovery api
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();

//          A: find all instances within designed server
            List<Instance> instances = namingService.selectInstances(name, true);

//          B: filter all instances by cluster
            List<Instance> sameClusterInstances = instances.stream()
                    .filter(instance -> Objects.equals(clusterName, instance.getClusterName()))
                    .collect(Collectors.toList());

//          choose A if B is null
            List<Instance> instancesToBeChosen = new ArrayList<>();
            if (CollectionUtils.isEmpty(sameClusterInstances)) {
                instancesToBeChosen = instances;
                log.warn("发生跨集群调用： name = {}, clusterName = {}, instances = {}",
                        name,
                        clusterName,
                        instances
                );
            } else {
                instancesToBeChosen = sameClusterInstances;

            }
//          return an instance based on weighted load balancing algorithms
            Instance instance = ExtendBalancer.getHostByRandomWeight2(instancesToBeChosen);
            log.info("选择的实例是：port = {}, instance = {}",
                    instance.getPort(),
                    instance
            );

            return new NacosServer(instance);

        } catch (NacosException e) {
            log.error("发生异常了", e);
            return null;
        }
    }
}

class ExtendBalancer extends Balancer {
    public static Instance getHostByRandomWeight2(List<Instance> hosts) {
        return getHostByRandomWeight(hosts);
    }
}
