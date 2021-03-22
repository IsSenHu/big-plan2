package com.gapache.sentinel.cluster.init;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gapache.sentinel.cluster.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * @author HuSen
 * @since 2021/3/16 4:46 下午
 */
@Slf4j
public class ClusterServerInitFunc implements InitFunc {

    private final String remoteAddress = "159.75.109.113:8848";
    private final String groupId = "SENTINEL_GROUP";
    private final String namespaceSetDataId = "cluster-server-namespace-set";
    private final String serverTransportDataId = "cluster-server-transport-config";

    @Override
    public void init() {
        // Register cluster flow rule property supplier which creates data source by namespace.
        log.info(">>>>>> Register cluster flow rule property supplier which creates data source by namespace.");
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                    namespace + Constants.FLOW_POSTFIX,
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                    }));
            return ds.getProperty();
        });
        // Register cluster parameter flow rule property supplier.
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                    namespace + Constants.PARAM_FLOW_POSTFIX,
                    source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {
                    }));
            return ds.getProperty();
        });

        // Server namespace set (scope) data source.
        ReadableDataSource<String, Set<String>> namespaceDs = new NacosDataSource<>(remoteAddress, groupId,
                namespaceSetDataId, source -> JSON.parseObject(source, new TypeReference<Set<String>>() {
        }));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());
        // Server transport configuration data source.
        ReadableDataSource<String, ServerTransportConfig> transportConfigDs = new NacosDataSource<>(remoteAddress,
                groupId, serverTransportDataId,
                source -> JSON.parseObject(source, new TypeReference<ServerTransportConfig>() {
                }));
        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getProperty());
    }
}
