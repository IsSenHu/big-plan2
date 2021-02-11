package com.gapache.job.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.api.config.ConfigService;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.commons.utils.BeanCopyUtils;
import com.gapache.job.common.model.JobStatus;
import com.gapache.job.common.model.JobVO;
import com.gapache.job.common.model.TriggerTaskRequest;
import com.gapache.job.sdk.config.InnerServerProperties;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.dao.repository.JobGroupRepository;
import com.gapache.job.server.dao.repository.JobRepository;
import com.gapache.job.server.service.AbstractListeningConfigChangeService;
import com.gapache.job.server.service.JobService;
import com.gapache.job.server.trigger.JobDataMapBuilder;
import com.gapache.job.server.trigger.QuartzManager;
import com.gapache.job.server.trigger.TriggerJob;
import com.gapache.jpa.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author HuSen
 * @since 2021/2/4 4:45 下午
 */
@Slf4j
@Service
public class JobServiceImpl extends AbstractListeningConfigChangeService implements JobService {

    @Resource
    private JobRepository jobRepository;

    @Resource
    private JobGroupRepository jobGroupRepository;

    @Resource
    private InnerServerProperties properties;

    @Value("${spring.application.name}")
    private String applicationName;

    protected JobServiceImpl(ConfigService configService) {
        super(configService);
    }

    @Override
    public PageResult<JobVO> page(IPageRequest<JobVO> iPageRequest) {
        Pageable pageable = PageHelper.of(iPageRequest);
        Page<JobEntity> page = jobRepository.findAll(pageable);
        return PageResult.of(page.getTotalElements(),  po -> {
            JobVO vo = new JobVO();
            BeanUtils.copyProperties(po, vo);
            return vo;
        }, page.getContent());
    }

    @Override
    public Boolean changeStatus(JobVO vo) {
        // 这里的状态修改应该要同步到所有的JobServer节点
        Optional<JobEntity> byId = jobRepository.findById(vo.getId());
        if (!byId.isPresent()) {
            return false;
        }
        pushConfig(JSON.toJSONString(Lists.newArrayList(vo)));
        return true;
    }

    @Override
    public Boolean changeCron(JobVO vo) {
        // 这里的状态修改应该要同步到所有的JobServer节点
        Optional<JobEntity> byId = jobRepository.findById(vo.getId());
        if (!byId.isPresent()) {
            return false;
        }
        pushConfig(JSON.toJSONString(Lists.newArrayList(vo)));
        return true;
    }

    @Override
    public Boolean trigger(TriggerTaskRequest request) {
        // TODO

        return null;
    }

    @Override
    protected String dataId() {
        return applicationName + ":jobInfo.json";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected void execute(String configInfo) {
        log.info("execute {}", configInfo);
        if (StringUtils.isBlank(configInfo)) {
            return;
        }

        List<JobVO> jobVOList = JSONArray.parseArray(configInfo, JobVO.class);
        for (JobVO vo : jobVOList) {
            Optional<JobEntity> byId = jobRepository.findById(vo.getId());
            if (!byId.isPresent()) {
                continue;
            }
            JobEntity jobEntity = byId.get();

            // 停止或者启动任务
            if (JobStatus.STOP.equals(vo.getStatus()) && JobStatus.RUNNING.equals(jobEntity.getStatus())) {
                Optional<JobGroupEntity> jobGroupEntity = jobGroupRepository.findById(jobEntity.getJobGroupId());
                jobGroupEntity.ifPresent(jobGroup -> QuartzManager.removeJob(jobGroup.getAppName(), jobEntity.getName()));
            } else if (JobStatus.RUNNING.equals(vo.getStatus()) && JobStatus.STOP.equals(jobEntity.getStatus())) {
                Optional<JobGroupEntity> jobGroupEntity = jobGroupRepository.findById(jobEntity.getJobGroupId());
                jobGroupEntity.ifPresent(jobGroup -> {
                    JobDataMap jobDataMap = new JobDataMapBuilder()
                            .set("job", jobEntity)
                            .set("group", jobGroup)
                            .set("port", properties.getPort())
                            .set("params", "")
                            .build();
                    QuartzManager.addJob(
                            jobGroup.getAppName(),
                            jobEntity.getName(),
                            jobEntity.getDescription(),
                            jobEntity.getCron(),
                            TriggerJob.class,
                            jobDataMap
                    );
                });
            }
            // 如果任务的cron表达式改变了，修改Job时间
            else if (JobStatus.RUNNING.equals(jobEntity.getStatus()) && !StringUtils.equalsIgnoreCase(jobEntity.getCron(), vo.getCron())) {
                Optional<JobGroupEntity> jobGroupEntity = jobGroupRepository.findById(jobEntity.getJobGroupId());
                jobGroupEntity.ifPresent(jobGroup -> QuartzManager.modifyJobTime(jobGroup.getAppName(), jobEntity.getName(), vo.getCron()));
            }

            BeanCopyUtils.copyIgnoreProperties(vo, jobEntity);
            jobRepository.save(jobEntity);
        }
    }
}
