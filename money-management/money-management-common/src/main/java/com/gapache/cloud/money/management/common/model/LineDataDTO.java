package com.gapache.cloud.money.management.common.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author HuSen
 * @since 2021/1/15 9:22 上午
 */
@Setter
@Getter
@ToString
public class LineDataDTO<X, Y> implements Serializable {
    private static final long serialVersionUID = -2525295764240262318L;

    /**
     * X轴数据
     */
    private List<X> xList;

    /**
     * Y轴数据
     */
    private List<Y> yList;

    /**
     * 其他需要的信息
     */
    private Map<String, Object> otherInfo;
}
