package com.gapache.job.common.model;

import com.dyuproject.protostuff.Tag;
import lombok.Data;

/**
 * @author HuSen
 * @since 2021/2/4 10:23 上午
 */
@Data
public class ClientInfo {

    @Tag(1)
    private String clientId;

    @Tag(2)
    private String appName;

    @Tag(3)
    private String name;
}
