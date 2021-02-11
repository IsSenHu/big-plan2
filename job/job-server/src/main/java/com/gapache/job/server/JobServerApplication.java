package com.gapache.job.server;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.gapache.security.annotation.EnableAuthResourceServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * 添加一个JobServer扫描器，如果有新增JobServer，就尝试建立连接
 * 阻塞处理策略：调度过于密集执行器来不及处理时的处理策略，策略包括：单机串行（默认）、丢弃后续调度、覆盖之前调度；
 * 如果一个JobServer节点确实不存在了，不要再重试连接了，抛弃它
 * 任务失败重试：支持自定义任务失败重试次数，当任务失败时将会按照预设的失败重试次数主动进行重试；
 * 其中分片任务支持分片粒度的失败重试；
 * 任务失败告警；默认提供邮件方式失败告警，同时预留扩展接口，可方便的扩展短信、钉钉等告警方式；
 * 路由策略：执行器集群部署时提供丰富的路由策略，包括：第一个、最后一个、轮询、随机；
 * 分片广播任务：执行器集群部署时，任务路由策略选择"分片广播"情况下，一次任务调度将会广播触发集群中所有执行器执行一次任务，可根据分片参数开发分片任务；
 * TODO HTTP调度模式
 * 数据加密：调度中心和执行器之间的通讯进行数据加密，提升调度信息安全性；
 * 上报NACOS的数据加解密
 * TODO Restful Api开发
 *
 * 这个时候只要JobServer至少有一台存活，一样可以调度任务过来，不影响任务的调度。
 *
 * @author HuSen
 * @since 2021/2/3 1:18 下午
 */
@EnableAuthResourceServer("JobServer")
@EnableDiscoveryClient
@SpringBootApplication
public class JobServerApplication {

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    public String serverAddr;

    @Bean
    public NamingService namingService() throws NacosException {
        return NacosFactory.createNamingService(serverAddr);
    }

    @Bean
    public ConfigService configService() throws NacosException {
        return NacosFactory.createConfigService(serverAddr);
    }

    public static void main(String[] args) {
        SpringApplication.run(JobServerApplication.class, args);
    }
}
