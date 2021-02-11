package com.gapache.job.server.callback;

import com.gapache.commons.utils.ContextUtils;
import com.gapache.job.common.Callback;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.TaskResult;
import com.gapache.job.common.utils.CallbackCache;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.job.server.dao.repository.JobGroupRepository;
import com.gapache.job.server.dao.repository.JobLogRepository;
import com.gapache.job.server.dao.repository.JobRepository;
import com.gapache.job.server.warner.Warner;
import com.gapache.job.server.warner.WarnerUtils;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author HuSen
 * @since 2021/2/4 4:39 下午
 */
@Slf4j
public class TaskResultCallback implements Callback {

    private final int retryTimes;

    public TaskResultCallback(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public void callback(ClientMessage message) {
        if (message.getType() == ClientMessage.Type.RESULT.getType()) {
            byte[] data = message.getData();
            ApplicationContext applicationContext = ContextUtils.getApplicationContext();
            JobLogRepository jobLogRepository = applicationContext.getBean(JobLogRepository.class);
            TaskResult taskResult = ProtocstuffUtils.byte2Bean(data, TaskResult.class);
            JobLogEntity jobLog = jobLogRepository.findByMessageId(taskResult.getMessageId());
            final int times = 10;
            int i = 0;
            while (jobLog == null && i++ < times) {
                jobLog = jobLogRepository.findByMessageId(taskResult.getMessageId());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ignored) {}
            }

            if (jobLog == null) {
                return;
            }
            // 失败重试，记录重试的次数，并且重新发送该消息
            if (taskResult.getCode() != TaskResult.SUCCESS && jobLog.getRetryTimes() < retryTimes) {
                jobLog.setRetryTimes(jobLog.getRetryTimes() + 1);
                CallbackCache.get("fail:" + message.getMessageId()).callback(message);
                // 再把自己保存回去
                CallbackCache.save(message.getMessageId(), this);
            } else {
                jobLog.setExecutorResult(taskResult.getCode() == TaskResult.SUCCESS);
                jobLog.setExecutorTime(taskResult.getExecutorTime());
                jobLog.setExecutorRemark(taskResult.getRemark());

                // 执行失败的话需要warning
                if (!jobLog.getExecutorResult()) {
                    JobRepository jobRepository = applicationContext.getBean(JobRepository.class);
                    Optional<JobEntity> job = jobRepository.findById(jobLog.getJobId());
                    String params = jobLog.getParams();
                    job.ifPresent(j -> {
                        JobGroupRepository jobGroupRepository = applicationContext.getBean(JobGroupRepository.class);
                        Optional<JobGroupEntity> jobGroup = jobGroupRepository.findById(j.getJobGroupId());
                        jobGroup.ifPresent(g -> {
                            String localPort = applicationContext.getEnvironment().getProperty("com.gapache.job.executor.local-port");
                            if (StringUtils.isBlank(localPort)) {
                                localPort = applicationContext.getEnvironment().getProperty("com.gapache.job.executor.localPort");
                            }
                            WarnerUtils.warning(g, j, params, StringUtils.isBlank(localPort) ? 9999 : Integer.parseInt(localPort), taskResult.getRemark());
                        });
                    });
                }
                // 移除不需要的failCallBack
                CallbackCache.get("fail:" + message.getMessageId());
            }
            jobLogRepository.save(jobLog);
        }
    }
}
