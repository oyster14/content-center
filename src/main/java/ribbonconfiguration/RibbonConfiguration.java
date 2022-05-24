package ribbonconfiguration;

import com.alibaba.cloud.nacos.ribbon.NacosRule;
import com.guanshi.contentcenter.configuration.NacosSameClusterWeightedRule;
import com.guanshi.contentcenter.configuration.NacosWeightedRule;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RibbonConfiguration {
    @Bean
    public IRule ribbonRule() {
        return new NacosRule();
    }
}
