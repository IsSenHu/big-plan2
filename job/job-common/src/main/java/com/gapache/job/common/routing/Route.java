package com.gapache.job.common.routing;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 路由接口
 *
 * @author HuSen
 * @since 2021/2/8 10:16 上午
 */
public interface Route {

    /**
     * 选择节点的抽象接口
     * 由子类实现选择策略
     *
     * @param instances 服务节点
     * @return 选择到的节点实例
     */
    Instance select(List<Instance> instances);
}
