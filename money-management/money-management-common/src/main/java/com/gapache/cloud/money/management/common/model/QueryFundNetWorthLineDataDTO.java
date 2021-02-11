package com.gapache.cloud.money.management.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author HuSen
 * @since 2021/1/15 9:26 上午
 */
@Setter
@Getter
@ToString
public class QueryFundNetWorthLineDataDTO implements Serializable {
    private static final long serialVersionUID = -1835678174104363768L;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate start;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate end;

    @NotBlank
    private String code;

    @NotBlank
    private String netWorthType;

    @NonNull
    private Boolean isNumber;
}
