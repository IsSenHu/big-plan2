package com.gapache.job.sdk;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.utils.IpUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HuSen
 * @since 2021/2/5 2:20 下午
 */
public final class JobServerRepository {

    private static final Set<String> JOB_SERVER_HISTORY = new HashSet<>();
    private static final List<Instance> INSTANCES = new ArrayList<>();

    private JobServerRepository() {}

    public static boolean isNew(Instance instance) {
        return !JOB_SERVER_HISTORY.contains(IpUtil.getIpPort(instance.getIp(), instance.getPort()));
    }

    public static void saveAll(List<Instance> instances) {
        instances.forEach(instance -> {
            JOB_SERVER_HISTORY.add(IpUtil.getIpPort(instance.getIp(), instance.getPort()));
            INSTANCES.removeIf(old -> StringUtils.equals(IpUtil.getIpPort(old.getIp(), old.getPort()), IpUtil.getIpPort(instance.getIp(), instance.getPort())));
            INSTANCES.add(instance);
        });
    }

    public static void remove(String ip, int port) {
        JOB_SERVER_HISTORY.remove(IpUtil.getIpPort(ip, port));
        INSTANCES.removeIf(instance -> StringUtils.equals(IpUtil.getIpPort(ip, port), IpUtil.getIpPort(instance.getIp(), instance.getPort())));
    }
}
