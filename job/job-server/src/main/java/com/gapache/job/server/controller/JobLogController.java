package com.gapache.job.server.controller;

import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.JsonResult;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.JobLogVO;
import com.gapache.job.server.service.JobLogService;
import com.gapache.security.annotation.AuthResource;
import com.gapache.security.annotation.NeedAuth;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author HuSen
 * @since 2021/2/5 4:35 下午
 */
@RestController
@NeedAuth("JobLog")
@RequestMapping("/api/jobLog")
public class JobLogController {

    @Resource
    public JobLogService jobLogService;

    @PostMapping("/page")
    @AuthResource(scope = "page", name = "分页查询JobLog")
    public JsonResult<PageResult<JobLogVO>> page(@RequestBody IPageRequest<JobLogVO> iPageRequest) {
        return JsonResult.of(jobLogService.page(iPageRequest));
    }
}
