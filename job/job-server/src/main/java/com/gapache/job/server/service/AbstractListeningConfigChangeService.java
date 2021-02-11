package com.gapache.job.server.service;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * @author HuSen
 * @since 2021/2/8 4:20 下午
 */
public abstract class AbstractListeningConfigChangeService implements SmartInitializingSingleton {

    private static final String GROUP = "LISTENING_MONITOR";
    private static final String INIT = "[]";

    protected final ConfigService configService;

    protected AbstractListeningConfigChangeService(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        String dataId = dataId();
        try {
            if (configService.removeConfig(dataId, GROUP) && configService.publishConfig(dataId, GROUP, INIT)) {
                configService.addListener(dataId, GROUP, new AbstractListener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        execute(configInfo);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void pushConfig(String content) {
        try {
            configService.publishConfig(dataId(), GROUP, content);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置文件ID
     *
     * @return 配置文件ID
     */
    protected abstract String dataId();

    /**
     * 接收到变化的配置
     *
     * @param configInfo configInfo
     */
    protected abstract void execute(String configInfo);
}
