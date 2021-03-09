package com.gapache.job.server.server;

import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;

/**
 * @author HuSen
 * @since 2021/3/4 2:56 下午
 */
public interface ServerMessageSender {

    String GROUP = "EXECUTOR";

    /**
     * 发送消息到Executor
     *
     * @param job     job
     * @param group   group
     * @param version version
     * @param params  params
     * @param port    port
     */
    void sendMessageToDestination(JobEntity job, JobGroupEntity group, String version, String params, int port);

    /**
     * 广播
     *
     * @param job     job
     * @param group   group
     * @param version version
     * @param params  params
     * @param port    port
     */
    void broadcastMessage(JobEntity job, JobGroupEntity group, String version, String params, int port);
}
