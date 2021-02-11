package com.gapache.job.server.server;

import com.gapache.commons.security.RSAUtils;
import com.gapache.commons.utils.IStringUtils;
import com.gapache.job.common.EncryptAndDecryptMessageHandler;
import com.gapache.job.common.model.Constants;
import com.gapache.job.common.utils.OsUtils;
import com.gapache.job.sdk.config.InnerServerProperties;
import com.gapache.job.common.thread.JobThreadBuilder;
import com.gapache.job.server.discovery.ExecutorDiscovery;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author HuSen
 * @since 2021/2/3 5:55 下午
 */
@Slf4j
@Component
public class InnerServer implements SmartLifecycle {

    private final InnerServerProperties properties;

    @Resource
    private ExecutorDiscovery executorDiscovery;

    private final PrivateKey privateKey;

    private final PublicKey publicKey;

    public InnerServer(InnerServerProperties properties) {
        this.properties = properties;
        String toExecutorPrivateKey = this.properties.getToExecutorPrivateKey();
        Assert.hasText(toExecutorPrivateKey, "toExecutorPrivateKey must need");
        String fromExecutorPublicKey = this.properties.getFromExecutorPublicKey();
        Assert.hasText(fromExecutorPublicKey, "fromExecutorPublicKey must need");

        try {
            byte[] toExecutorPrivateKeyBytes = FileCopyUtils.copyToByteArray(new ClassPathResource(toExecutorPrivateKey).getInputStream());
            this.privateKey = RSAUtils.getPrivateKey(IStringUtils.newString(toExecutorPrivateKeyBytes));

            byte[] fromExecutorPublicKeyBytes = FileCopyUtils.copyToByteArray(new ClassPathResource(fromExecutorPublicKey).getInputStream());
            this.publicKey = RSAUtils.getPublicKey(IStringUtils.newString(fromExecutorPublicKeyBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean running;

    private ThreadPoolExecutor threadPoolExecutor;

    private ChannelInitializer<SocketChannel> createSocketChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ByteBuf delimiter = Unpooled.copiedBuffer(Constants.DELIMITER.getBytes());
                socketChannel.pipeline()
                        .addLast(new EncryptAndDecryptMessageHandler(privateKey, publicKey))
                        .addLast(new DelimiterBasedFrameDecoder(4096, delimiter))
                        .addLast(new ClientMessageDecodeHandler())
                        .addLast(new ServerMessageEncodeHandler())
                        .addLast(new ClientMessageHandler(threadPoolExecutor, executorDiscovery));
            }
        };
    }

    @Override
    public void start() {
        starting();
    }

    private synchronized void starting() {
        if (this.running) {
            return;
        }
        log.info(">>>>>>>>>>> InnerServer starting");
        this.running = true;

        threadPoolExecutor = new ThreadPoolExecutor(0, 2, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r, "InnerServer-biz-thread")
                , new ThreadPoolExecutor.CallerRunsPolicy());

        String osName = OsUtils.osName();
        boolean useEpoll = OsUtils.isLinux(osName);

        if (useEpoll) {
            log.info(">>>>>>>>>>> job remoting server use epoll");
        }

        new JobThreadBuilder()
                .create(() -> {
            EventLoopGroup bossGroup = useEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
            EventLoopGroup workerGroup = useEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
            Class<? extends ServerChannel> channelClass = useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

            try {
                ServerBootstrap bootstrap = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(channelClass)
                        .childHandler(createSocketChannelInitializer())
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                // bind
                ChannelFuture future = bootstrap.bind(properties.getPort()).sync();

                log.info(">>>>>>>>>>> job remoting server start success, netty type = {}, port = {}", InnerServer.class, properties.getPort());

                // wait util stop
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    log.info(">>>>>>>>>>> job remoting server stop.");
                } else {
                    log.error(">>>>>>>>>>> job remoting server error.", e);
                }
            } finally {
                // stop
                try {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        })
                .daemon(true)
                .buildAndStart();
    }

    @Override
    public void stop() {
        shutdown();
    }

    private synchronized void shutdown() {
        log.info(">>>>>>>>>>> InnerServer shutdown");
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
