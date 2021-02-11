package com.gapache.job.server.service;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.ClientInfo;
import com.gapache.job.common.model.JobGroupVO;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/2/4 10:42 上午
 */
public interface JobGroupService {

    /**
     * 检查和保存并且监听
     *
     * @param clientInfo clientInfo
     * @param instances  服务实例
     */
    void checkAndSave(ClientInfo clientInfo, List<Instance> instances);

    /**
     * 监听到服务的变化
     *
     * @param serviceName 服务名
     * @param instances   服务实例
     */
    void listening(String serviceName, List<Instance> instances);

    /**
     * 分页查询
     *
     * @param iPageRequest iPageRequest
     * @return 分页结果
     */
    PageResult<JobGroupVO> page(IPageRequest<JobGroupVO> iPageRequest);
}
