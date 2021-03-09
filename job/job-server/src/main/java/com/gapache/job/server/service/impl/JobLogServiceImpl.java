package com.gapache.job.server.service.impl;

import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.JobLogVO;
import com.gapache.job.server.service.JobLogService;
import com.gapache.vertx.redis.support.SimpleRedisRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @author HuSen
 * @since 2021/2/4 4:45 下午
 */
@Service
public class JobLogServiceImpl implements JobLogService {

    @Resource
    private SimpleRedisRepository simpleRedisRepository;

    @Override
    public PageResult<JobLogVO> page(IPageRequest<JobLogVO> iPageRequest) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        PageResult<JobLogVO> pageResult = new PageResult<>();
        simpleRedisRepository.page(iPageRequest, JobLogVO.class)
                .onSuccess(p -> {
                    BeanUtils.copyProperties(p, pageResult);
                    countDownLatch.countDown();
                });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pageResult;
    }
}
