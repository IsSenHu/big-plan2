package com.gapache.cloud.money.management.common.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author HuSen
 * @since 2021/1/14 10:01 上午
 */
@Setter
@Getter
@ToString
public class FundNetWorthDTO implements Serializable {
    private static final long serialVersionUID = -2604403320307201123L;

    private Long id;

    private String code;

    private Double netWorth;

    private Double addUpNetWorth;

    private LocalDate day;
}
