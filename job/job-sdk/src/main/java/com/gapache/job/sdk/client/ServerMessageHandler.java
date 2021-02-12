package com.gapache.job.sdk.client;

import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.common.model.TaskResult;
import com.gapache.job.sdk.JobTrigger;
import com.gapache.job.sdk.ZzhJobRepository;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author HuSen
 * @since 2021/2/4 1:45 下午
 */
@Slf4j
public class ServerMessageHandler extends SimpleChannelInboundHandler<ServerMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ServerMessage serverMessage) {
        int type = serverMessage.getType();
        ServerMessage.Type check = ServerMessage.Type.check(type);
        if (check == null) {
            return;
        }
        switch (check) {
            case TASK: {
                TaskInfo taskInfo = ProtocstuffUtils.byte2Bean(serverMessage.getData(), TaskInfo.class);
                TaskExecutor taskExecutor = new TaskExecutor(channelHandlerContext, taskInfo, serverMessage.getMessageId());

                switch (taskInfo.getBlockingStrategy()) {
                    case COVER: {
                        // 清空队列。
                        TaskPuller.clear(taskInfo.getName());
                        // 覆盖之前的任务阻塞策略
                        TaskPuller.push(taskInfo.getName(), taskExecutor);
                        break;
                    }
                    case DISCARD: {
                        // 丢弃后续的任务
                        if (!TaskPuller.isEmpty(taskInfo.getName())) {
                            break;
                        }
                        TaskPuller.push(taskInfo.getName(), taskExecutor);
                        break;
                    }
                    default: {
                        // 将任务放进队列里面-默认的单机串行
                        TaskPuller.push(taskInfo.getName(), taskExecutor);
                    }
                }
                break;
            }
            case CLOSE:
            default:
        }
    }

    @Slf4j
    public static class TaskPuller {

        private static final ConcurrentMap<String, LinkedBlockingQueue<TaskExecutor>> QUEUE_MAP = new ConcurrentHashMap<>();

        private static void push(String name, TaskExecutor taskExecutor) {
            LinkedBlockingQueue<TaskExecutor> executorQueue = QUEUE_MAP.computeIfAbsent(name, key -> new LinkedBlockingQueue<>());
            if (executorQueue.offer(taskExecutor)) {
                if (log.isDebugEnabled()) {
                    log.debug("push {} task in queue", name);
                }
            }
        }

        private static boolean isEmpty(String name) {
            return QUEUE_MAP.get(name) == null || QUEUE_MAP.get(name).isEmpty();
        }

        public static TaskExecutor take(String name) {
            try {
                LinkedBlockingQueue<TaskExecutor> executorQueue = QUEUE_MAP.get(name);
                if (executorQueue == null) {
                    return null;
                }
                return executorQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private static void clear(String name) {
            LinkedBlockingQueue<TaskExecutor> executorQueue = QUEUE_MAP.get(name);
            if (executorQueue != null) {
                executorQueue.clear();
            }
        }
    }

    public static class TaskExecutor implements Runnable {

        private final ChannelHandlerContext channelHandlerContext;
        private final TaskInfo taskInfo;
        private final String messageId;

        public TaskExecutor(ChannelHandlerContext channelHandlerContext, TaskInfo taskInfo, String messageId) {
            this.channelHandlerContext = channelHandlerContext;
            this.taskInfo = taskInfo;
            this.messageId = messageId;
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
                channelHandlerContext.writeAndFlush(clientMessage);
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
            channelHandlerContext.writeAndFlush(clientMessage);
        }
    }
}
