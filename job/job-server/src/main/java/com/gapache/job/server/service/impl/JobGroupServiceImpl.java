package com.gapache.job.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.ClientInfo;
import com.gapache.job.common.model.JobGroupVO;
import com.gapache.job.common.model.JobInfo;
import com.gapache.job.common.model.JobStatus;
import com.gapache.job.server.server.config.InnerServerProperties;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.dao.repository.JobGroupRepository;
import com.gapache.job.server.dao.repository.JobRepository;
import com.gapache.job.server.service.JobGroupService;
import com.gapache.job.server.trigger.JobDataMapBuilder;
import com.gapache.job.server.trigger.QuartzManager;
import com.gapache.job.server.trigger.TriggerJob;
import com.gapache.jpa.PageHelper;
import org.quartz.JobDataMap;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author HuSen
 * @since 2021/2/4 10:42 上午
 */
@Service
public class JobGroupServiceImpl implements JobGroupService {

    @Resource
    private JobGroupRepository jobGroupRepository;

    @Resource
    private JobRepository jobRepository;

    @Resource
    private InnerServerProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndSave(ClientInfo clientInfo, List<Instance> instances) {
        JobGroupEntity jobGroup = jobGroupRepository.findByAppName(clientInfo.getAppName());
        if (null == jobGroup) {
            jobGroup = new JobGroupEntity();
            jobGroup.setAppName(clientInfo.getAppName());
            jobGroup.setName(clientInfo.getName());
        }

        Long jobGroupId = jobGroup.getId();

        if (instances.isEmpty()) {
            jobGroup.setAddressList(null);
        } else {
            String addressList = instances.stream().map(instance -> instance.getIp() + ":" + instance.getPort()).collect(Collectors.joining(","));
            jobGroup.setAddressList(addressList);
        }

        jobGroupRepository.save(jobGroup);
        jobGroupId = jobGroup.getId();

        saveJobs(instances, jobGroup, jobGroupId);
    }

    private void saveJobs(List<Instance> instances, JobGroupEntity jobGroup, Long jobGroupId) {
        if (!instances.isEmpty()) {
            Instance instance = instances.get(0);
            String jobs = instance.getMetadata().get("jobs");
            List<JobInfo> jobInfos = JSONArray.parseArray(jobs, JobInfo.class);
            Map<String, JobInfo> nameJobInfoMap = jobInfos.stream().collect(Collectors.toMap(JobInfo::getName, job -> job));

            List<JobEntity> jobEntities = jobRepository.findAllByJobGroupId(jobGroupId);

            Map<String, JobEntity> nameJobMap = jobEntities.stream().collect(Collectors.toMap(JobEntity::getName, job -> job));

            Map<Boolean, List<JobEntity>> existedMap = jobEntities.stream().collect(Collectors.groupingBy(job -> nameJobInfoMap.containsKey(job.getName())));

            if (existedMap.get(false) != null && !existedMap.get(false).isEmpty()) {
                jobRepository.deleteAll(existedMap.get(false));
                existedMap.get(false).forEach(job -> {
                    if (JobStatus.RUNNING.equals(job.getStatus())) {
                        QuartzManager.removeJob(jobGroup.getAppName(), job.getName());
                    }
                });
            }

            List<JobEntity> update = existedMap.get(true);
            if (update != null) {
                update.forEach(job -> {
                    JobInfo jobInfo = nameJobInfoMap.get(job.getName());
                    job.setName(jobInfo.getName());
                    job.setAuthor(jobInfo.getAuthor());
                    job.setCron(jobInfo.getCron());
                    job.setDescription(jobInfo.getDescription());
                    job.setRetryTimes(jobInfo.getRetryTimes());
                    job.setRouteStrategy(jobInfo.getRouteStrategy());
                    job.setEmail(jobInfo.getEmail());
                });

                jobRepository.saveAll(update);
            }

            List<JobEntity> newAdd = jobInfos.stream().filter(info -> !nameJobMap.containsKey(info.getName()))
                    .map(info -> {
                        JobEntity jobEntity = new JobEntity();
                        BeanUtils.copyProperties(info, jobEntity);
                        jobEntity.setJobGroupId(jobGroupId);
                        return jobEntity;
                    }).collect(Collectors.toList());

            jobRepository.saveAll(newAdd);

            newAdd.forEach(job -> {
                if (JobStatus.STOP.equals(job.getStatus())) {
                    return;
                }
                JobDataMap jobDataMap = new JobDataMapBuilder()
                        .set("job", job)
                        .set("group", jobGroup)
                        .set("port", properties.getPort())
                        .set("params", "")
                        .build();
                QuartzManager.addJob(
                        jobGroup.getAppName(),
                        job.getName(),
                        job.getDescription(), job.getCron(),
                        TriggerJob.class, jobDataMap);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void listening(String serviceName, List<Instance> instances) {
        JobGroupEntity jobGroupEntity = jobGroupRepository.findByAppName(serviceName);
        if (jobGroupEntity == null) {
            return;
        }

        if (instances.isEmpty()) {
            jobGroupEntity.setAddressList(null);
        } else {
            String addressList = instances.stream().map(instance -> instance.getIp() + ":" + instance.getPort()).collect(Collectors.joining(","));
            jobGroupEntity.setAddressList(addressList);
        }
        jobGroupRepository.save(jobGroupEntity);

        saveJobs(instances, jobGroupEntity, jobGroupEntity.getId());
    }

    @Override
    public PageResult<JobGroupVO> page(IPageRequest<JobGroupVO> iPageRequest) {
        Pageable pageable = PageHelper.of(iPageRequest);
        Page<JobGroupEntity> page = jobGroupRepository.findAll(pageable);
        return PageResult.of(page.getTotalElements(), po -> {
            JobGroupVO vo = new JobGroupVO();
            BeanUtils.copyProperties(po, vo);
            return vo;
        } , page.getContent());
    }
}
