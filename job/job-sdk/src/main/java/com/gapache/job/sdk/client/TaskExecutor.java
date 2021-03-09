package com.gapache.job.sdk.client;

import com.alibaba.fastjson.JSON;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.common.model.TaskResult;
import com.gapache.job.sdk.JobTrigger;
import com.gapache.job.sdk.ZzhJobRepository;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * @author HuSen
 * @since 2021/3/4 2:27 下午
 */
@Slf4j
public class TaskExecutor implements Runnable {

    private final TaskInfo taskInfo;
    private final String messageId;
    private final Consumer<ClientMessage> clientMessageConsumer;

    public TaskExecutor(TaskInfo taskInfo, String messageId, Consumer<ClientMessage> clientMessageConsumer) {
        this.taskInfo = taskInfo;
        this.messageId = messageId;
        this.clientMessageConsumer = clientMessageConsumer;
    }

    @Override
    public void run() {
        JobTrigger jobTrigger = ZzhJobRepository.get(taskInfo.getName());
        ClientMessage clientMessage = new ClientMessage();
        clientMessage.setMessageId(messageId);

        if (jobTrigger == null) {
            clientMessage.setCode(5000);
            clientMessage.setError(String.format("%s任务不存在", taskInfo.getName()));
            clientMessage.setType(ClientMessage.Type.RESULT.getType());

            TaskResult taskResult = new TaskResult();
            taskResult.setCode(clientMessage.getCode());
            taskResult.setMessageId(clientMessage.getMessageId());
            taskResult.setRemark(clientMessage.getError());

            clientMessage.setData(ProtocstuffUtils.bean2Byte(taskInfo, TaskInfo.class));
            if (clientMessageConsumer != null) {
                clientMessageConsumer.accept(clientMessage);
            }
            return;
        }
        TaskResult taskResult = new TaskResult();
        taskResult.setMessageId(messageId);
        taskResult.setExecutorTime(LocalDateTime.now());
        try {
            boolean execute = jobTrigger.execute(taskInfo);
            if (execute) {
                taskResult.setCode(0);
                taskResult.setRemark("成功");
            } else {
                taskResult.setCode(5000);
                taskResult.setRemark("执行失败");
            }
        } catch (Exception e) {
            taskResult.setCode(5000);
            taskResult.setRemark(e.toString());
        }
        clientMessage.setCode(0);
        clientMessage.setType(ClientMessage.Type.RESULT.getType());
        clientMessage.setData(ProtocstuffUtils.bean2Byte(taskResult, TaskResult.class));
        if (clientMessageConsumer != null) {
            clientMessageConsumer.accept(clientMessage);
        }
    }

    public static final Handler<AsyncResult<ServerMessage>> LOGGER = event -> {
        if (event.succeeded()) {
            log.info("Zzh-job >>>>>> {}", JSON.toJSONString(event.result()));
        } else {
            log.error("Zzh-job >>>>>>", event.cause());
        }
    };
}
