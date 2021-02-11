package com.gapache.job.sdk.registry;

import org.springframework.context.ApplicationEvent;

import java.security.PrivateKey;

/**
 * @author HuSen
 * @since 2021/2/4 11:05 上午
 */
public class RegistryEvent extends ApplicationEvent {
    private static final long serialVersionUID = -7652458773600715761L;

    private final PrivateKey privateKey;

    private final int localPort;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source     the object on which the event initially occurred or with
     *                   which the event is associated (never {@code null})
     * @param localPort  localPort
     * @param privateKey privateKey
     */
    public RegistryEvent(Object source, PrivateKey privateKey, int localPort) {
        super(source);
        this.privateKey = privateKey;
        this.localPort = localPort;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public int getLocalPort() {
        return localPort;
    }
}
