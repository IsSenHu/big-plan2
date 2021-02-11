package com.gapache.job.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HuSen
 * @since 2021/2/5 1:03 下午
 */
public final class ZzhJobRepository {

    private static final Map<String, JobTrigger> CACHE = new HashMap<>(64);

    private ZzhJobRepository() {}

    public static JobTrigger get(String name) {
        return CACHE.get(name);
    }

    public static void save(String name, JobTrigger jobTrigger) {
        CACHE.put(name, jobTrigger);
    }
}
