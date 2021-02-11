package com.gapache.job.common;

import com.gapache.job.common.model.ClientMessage;

/**
 * @author HuSen
 * @since 2021/2/4 10:28 上午
 */
public interface Callback {

    /**
     * 回调
     *
     * @param message ClientMessage
     */
    void callback(ClientMessage message);
}
