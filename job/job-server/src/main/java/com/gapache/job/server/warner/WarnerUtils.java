package com.gapache.job.server.warner;

import com.gapache.commons.utils.ContextUtils;
import com.gapache.commons.utils.TimeUtils;
import com.gapache.job.common.utils.IpUtil;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2021/2/8 3:02 下午
 */
public class WarnerUtils {

    public static void warning(JobGroupEntity group, JobEntity job, String params, int port, String error) {
        if (StringUtils.isBlank(job.getEmail())) {
            return;
        }
        Warner warner = ContextUtils.getApplicationContext().getBean(Warner.class);
        String subject = group.getAppName() + "." + job.getName() + ":" + job.getDescription() + "执行失败";

        String from = ContextUtils.getApplicationContext().getEnvironment().getProperty("spring.mail.username");
        String textBuilder = "调度节点: " + group.getAppName() + "\r\n" +
                "失败原因: " + error + "\r\n" +
                "调度参数: " + params + "\r\n" +
                "调度中心: " + IpUtil.getIp() + ":" + port + "\r\n" +
                "调度时间: " + TimeUtils.format(TimeUtils.Format._2, LocalDateTime.now());

        warner.warning(subject, textBuilder, job.getEmail(), from);
    }
}
