package com.gapache.job.server.controller;

import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.JsonResult;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.JobGroupVO;
import com.gapache.job.server.service.JobGroupService;
import com.gapache.security.annotation.AuthResource;
import com.gapache.security.annotation.NeedAuth;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author HuSen
 * @since 2021/2/5 4:21 下午
 */
@RestController
@NeedAuth("JobGroup")
@RequestMapping("/api/jobGroup")
public class JobGroupController {

    @Resource
    private JobGroupService jobGroupService;

    @PostMapping("/page")
    @AuthResource(scope = "page", name = "分页查询JobGroup")
    public JsonResult<PageResult<JobGroupVO>> page(@RequestBody IPageRequest<JobGroupVO> iPageRequest) {
        return JsonResult.of(jobGroupService.page(iPageRequest));
    }
}
