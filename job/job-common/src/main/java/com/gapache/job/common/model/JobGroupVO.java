package com.gapache.job.common.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author HuSen
 * @since 2021/2/5 4:23 下午
 */
@Setter
@Getter
@ToString
public class JobGroupVO implements Serializable {
    private static final long serialVersionUID = 4373367273482922766L;

    private Long id;

    private String appName;

    private String name;

    private String addressList;
}
