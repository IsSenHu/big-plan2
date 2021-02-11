package com.gapache.cloud.money.management.common.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author HuSen
 * @since 2021/1/13 2:52 下午
 */
@Data
public class FundDTO implements Serializable {
    private static final long serialVersionUID = -6666192430556399820L;

    private Long id;

    /**
     * 基金代码
     */
    private String code;
    /**
     * 基金名称
     */
    private String name;

    /**
     * 持有
     */
    private Boolean hold;
    /**
     * 自选
     */
    private Boolean optional;
}
