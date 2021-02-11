package com.gapache.job.server.trigger;

import org.quartz.JobDataMap;

/**
 * @author HuSen
 * @since 2021/2/8 4:11 下午
 */
public class JobDataMapBuilder {

    private final JobDataMap jobDataMap;

    public JobDataMapBuilder() {
        jobDataMap = new JobDataMap();
    }

    public JobDataMapBuilder set(String name, Object object) {
        jobDataMap.put(name, object);
        return this;
    }

    public JobDataMap build() {
        return jobDataMap;
    }
}
