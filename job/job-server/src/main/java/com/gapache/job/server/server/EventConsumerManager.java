package com.gapache.job.server.server;

import com.alibaba.fastjson.JSON;
import com.gapache.commons.utils.ContextUtils;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.job.server.dao.repository.JobGroupRepository;
import com.gapache.job.server.dao.repository.JobRepository;
import com.gapache.job.server.server.zeus.EventConsumerVerticle;
import com.gapache.job.server.warner.WarnerUtils;
import com.gapache.vertx.core.VertxManager;
import com.gapache.vertx.redis.support.SimpleRedisRepository;
import io.vertx.core.DeploymentOptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author HuSen
 * @since 2021/3/9 11:02 上午
 */
@Component
public class EventConsumerManager implements InitializingBean {
    public static final String JOB_FINALLY_RESULT_DEAL_ADDRESS = "job.finally.result.deal.address";

    @Override
    public void afterPropertiesSet() {
        EventConsumerVerticle<String> jobFinallyResultDeal = new EventConsumerVerticle<>(JOB_FINALLY_RESULT_DEAL_ADDRESS, event -> {
            JobLogEntity jobLog = JSON.parseObject(event.body(), JobLogEntity.class);
            ApplicationContext applicationContext = ContextUtils.getApplicationContext();
            SimpleRedisRepository simpleRedisRepository = applicationContext.getBean(SimpleRedisRepository.class);
            // 保存日志
            simpleRedisRepository.save(jobLog);

            JobRepository jobRepository = applicationContext.getBean(JobRepository.class);
            if (!jobLog.getExecutorResult()) {
                // 改异步，使用事件发布的模式
                Optional<JobEntity> job = jobRepository.findById(jobLog.getJobId());
                String params = jobLog.getParams();
                job.ifPresent(j -> {
                    JobGroupRepository jobGroupRepository = applicationContext.getBean(JobGroupRepository.class);
                    Optional<JobGroupEntity> jobGroup = jobGroupRepository.findById(j.getJobGroupId());
                    jobGroup.ifPresent(g -> {
                        String localPort = applicationContext.getEnvironment().getProperty("com.gapache.job.server.port");
                        WarnerUtils.warning(g, j, params, StringUtils.isBlank(localPort) ? 9999 : Integer.parseInt(localPort), jobLog.getExecutorRemark());
                    });
                });
            }
        });
        VertxManager.getVertx().deployVerticle(jobFinallyResultDeal, new DeploymentOptions().setWorker(true));
    }
}
