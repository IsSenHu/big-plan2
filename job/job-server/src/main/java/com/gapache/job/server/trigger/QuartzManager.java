package com.gapache.job.server.trigger;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author HuSen
 * @since 2021/2/3 5:40 下午
 */
@Slf4j
public class QuartzManager {

    private static final SchedulerFactory SCHEDULER_FACTORY = new StdSchedulerFactory();

    public static void addJob(String group, String name, String description, String cron, Class<? extends Job> jobClass, JobDataMap jobDataMap) {
        try {
            log.info("add job {} {} {}", group, name, jobDataMap);
            Scheduler scheduler = SCHEDULER_FACTORY.getScheduler();

            JobDetail jobDetail = JobBuilder
                    .newJob(jobClass)
                    .withIdentity(name, group)
                    .setJobData(jobDataMap).withDescription(description)
                    .build();

            TriggerBuilder<CronTrigger> triggerBuilder = TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .startNow();

            CronTrigger cronTrigger = triggerBuilder.build();
            // money-management-server.money-management-server:AutoCrawlingFundDataJob
            log.info("cronTrigger {}", cronTrigger.getKey());

            scheduler.scheduleJob(jobDetail, cronTrigger);

            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modifyJobTime(String group, String name, String cron) {

        try {
            Scheduler scheduler = SCHEDULER_FACTORY.getScheduler();
            // money-management-server.AutoCrawlingFundDataJob

            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            TriggerBuilder<CronTrigger> builder = triggerBuilder
                    .withIdentity(name, group)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .startNow();

            CronTrigger newTrigger = builder.build();
            TriggerKey triggerKey = newTrigger.getKey();
            Trigger trigger = scheduler.getTrigger(triggerKey);
            log.info("modifyJobTime job {} {} {} {} {}", group, name, cron, triggerKey, trigger);
            if (trigger == null) {
                return;
            }
            // 方式一：修改一个任务的触发时间
            scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeJob(String group, String name) {
        // 什么情况下可以删除
        try {
            Scheduler scheduler = SCHEDULER_FACTORY.getScheduler();

            TriggerKey triggerKey = TriggerKey.triggerKey(name, group);
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            scheduler.deleteJob(JobKey.jobKey(name, group));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
