package com.gapache.cloud.money.management.common.model;

import com.gapache.commons.model.Error;
import lombok.Getter;

/**
 * @author HuSen
 * @since 2020/8/13 5:16 下午
 */
@Getter
public enum MoneyManagementError implements Error {
    //
    FILE_EMPTY(20001, "文件为空"),
    FUND_NOT_FOUND(20002, "基金不存在");
    private final Integer code;
    private final String error;

    MoneyManagementError(Integer code, String error) {
        this.code = code;
        this.error = error;
    }
}
