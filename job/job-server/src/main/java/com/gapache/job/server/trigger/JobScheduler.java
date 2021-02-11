package com.gapache.job.server.trigger;

import com.gapache.job.common.model.JobStatus;
import com.gapache.job.sdk.config.InnerServerProperties;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.dao.repository.JobGroupRepository;
import com.gapache.job.server.dao.repository.JobRepository;
import org.quartz.JobDataMap;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HuSen
 * @since 2021/2/3 5:32 下午
 */
@Component
public class JobScheduler implements SmartLifecycle {

    private boolean running;

    @Resource
    private JobGroupRepository jobGroupRepository;

    @Resource
    private JobRepository jobRepository;

    @Resource
    private InnerServerProperties properties;

    @Override
    public void start() {
        starting();
    }

    private synchronized void starting() {
        if (this.running) {
            return;
        }
        // 默认会执行处于RUNNING状态的任务，并且以第一次配置为准，后续配置不会再覆盖
        // 比如一个任务刚开始默认是以RUNNING的状态，注册就可以运行
        // 但后续我们手动把这个任务关闭以后，就算下次再以RUNNING的状态注册过来，我们是不会管的
        // 因为我们任务手动关闭了这个任务，是我们真正想控制的动作
        // 所以只有当一个新的任务过来并且默认的状态时RUNNING的时候，我们才需要自动的开启这个任务
        List<JobGroupEntity> jobGroupList = jobGroupRepository.findAll();
        for (JobGroupEntity group : jobGroupList) {
            List<JobEntity> jobList = jobRepository.findAllByJobGroupId(group.getId());
            for (JobEntity job : jobList) {
                if (JobStatus.RUNNING.equals(job.getStatus())) {
                    JobDataMap jobDataMap = new JobDataMapBuilder()
                            .set("job", job)
                            .set("group", group)
                            .set("port", properties.getPort())
                            .set("params", "")
                            .build();
                    QuartzManager.addJob(
                            group.getAppName(),
                            job.getName(),
                            job.getDescription(), job.getCron(),
                            TriggerJob.class, jobDataMap);
                }
            }
        }
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}
