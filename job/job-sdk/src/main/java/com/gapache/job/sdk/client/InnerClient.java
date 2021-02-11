package com.gapache.job.sdk.client;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.commons.security.RSAUtils;
import com.gapache.commons.utils.IStringUtils;
import com.gapache.job.common.EncryptAndDecryptMessageHandler;
import com.gapache.job.common.model.Constants;
import com.gapache.job.common.thread.JobThreadBuilder;
import com.gapache.job.common.utils.IpUtil;
import com.gapache.job.common.utils.OsUtils;
import com.gapache.job.sdk.JobServerRepository;
import com.gapache.job.sdk.config.ExecutorRegistryProperties;
import com.gapache.job.sdk.registry.RegistryEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author HuSen
 * @since 2021/2/4 11:44 上午
 */
@Slf4j
public class InnerClient implements SmartLifecycle {

    private boolean running;

    private static final String GROUP = "EXECUTOR";

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosAddress;

    @Value("${spring.application.name}")
    private String appName;

    @Resource
    private ApplicationContext applicationContext;

    private final String name;

    private final String clientId;

    private final int localPort;

    private NamingService namingService;

    private final Map<String, ChannelFuture> futureMap;

    private final Map<String, EventLoopGroup> groupMap;

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public InnerClient(ExecutorRegistryProperties properties) {
        name = properties.getName();
        localPort = properties.getLocalPort() == 0 ? 9999 : properties.getLocalPort();
        Assert.hasText(name, "name must need");
        clientId = UUID.randomUUID().toString();
        futureMap = new HashMap<>(4);
        groupMap = new HashMap<>(4);

        String fromServerPublicKey = properties.getFromServerPublicKey();
        Assert.hasText(fromServerPublicKey, "fromServerPublicKey must need");
        String toServerPrivateKey = properties.getToServerPrivateKey();
        Assert.hasText(toServerPrivateKey, "toServerPrivateKey must need");

        try {
            byte[] fromServerPublicKeyBytes = FileCopyUtils.copyToByteArray(new ClassPathResource(fromServerPublicKey).getInputStream());
            this.publicKey = RSAUtils.getPublicKey(IStringUtils.newString(fromServerPublicKeyBytes));

            byte[] toServerPrivateKeyBytes = FileCopyUtils.copyToByteArray(new ClassPathResource(toServerPrivateKey).getInputStream());
            this.privateKey = RSAUtils.getPrivateKey(IStringUtils.newString(toServerPrivateKeyBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        starting();
    }

    private synchronized void starting() {
        if (this.running) {
            return;
        }
        log.info(">>>>>>>>>>> InnerClient starting");
        this.running = true;

        try {
            namingService = NacosFactory.createNamingService(nacosAddress);
            List<Instance> instances = namingService.getAllInstances("job-server", true);
            while (instances.isEmpty()) {
                TimeUnit.SECONDS.sleep(10L);
                instances = namingService.getAllInstances("job-server", true);
            }
            startClient(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void startClient(List<Instance> targetServer) {
        log.info(">>>>>> startClient {}", targetServer);
        targetServer.forEach(this::createByInstance);
        JobServerRepository.saveAll(targetServer);
        applicationContext.publishEvent(new RegistryEvent(clientId, privateKey, localPort));
    }

    public synchronized void createByInstance(Instance instance) {
        new JobThreadBuilder()
                .create(() -> {
                    final Bootstrap bootstrap = new Bootstrap();
                    final String childId = UUID.randomUUID().toString();
                    try {
                        EventLoopGroup group = OsUtils.isLinux(OsUtils.osName()) ? new EpollEventLoopGroup() : new NioEventLoopGroup();
                        groupMap.put(childId, group);
                        bootstrap
                                .group(group)
                                .channel(OsUtils.isLinux(OsUtils.osName()) ? EpollSocketChannel.class : NioSocketChannel.class)
                                .option(ChannelOption.SO_KEEPALIVE, true)
                                .handler(createChannelInitializer(instance))
                                .remoteAddress(instance.getIp(), Integer.parseInt(instance.getMetadata().get("server.port")));

                        // start server
                        ChannelFuture future = bootstrap.connect();
                        futureMap.put(childId, future);
                        log.info(">>>>>>>>>>> job remoting client start success, netty type = {}, port = {}", InnerClient.class, instance.getPort());
                    } catch (Exception e) {
                        log.error(">>>>>>>>>>> job remoting client stop.", e);
                    } finally {
                        // stop
                        try {
                            // wait util stop
                            ChannelFuture channelFuture = futureMap.get(childId);
                            if (channelFuture != null) {
                                channelFuture.channel().closeFuture().sync();
                            }
                            groupMap.get(childId).shutdownGracefully();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                })
                .buildAndStart();
    }

    private ChannelInitializer<SocketChannel> createChannelInitializer(Instance instance) {
        InnerClient embedClient = this;
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                ByteBuf delimiter = Unpooled.copiedBuffer(Constants.DELIMITER.getBytes());
                channel.pipeline()
                        .addLast(new EncryptAndDecryptMessageHandler(privateKey, publicKey))
                        .addLast(new DelimiterBasedFrameDecoder(4096, delimiter))
                        .addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))
                        .addLast(new ServerMessageDecodeHandler())
                        .addLast(new ClientMessageEncodeHandler())
                        .addLast(new ClientLifeCycleHandler(appName, embedClient, instance))
                        .addLast(new ServerMessageHandler());
            }
        };
    }

    @Override
    public void stop() {
        stopRegistry();
    }

    private synchronized void stopRegistry() {
        try {
            namingService.deregisterInstance(appName, GROUP, IpUtil.getIp(), localPort);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public String getName() {
        return name;
    }

    public String getClientId() {
        return clientId;
    }

    public void registry() {
        applicationContext.publishEvent(new RegistryEvent(clientId, privateKey, localPort));
    }
}
