package com.gapache.job.server.controller;

import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.JsonResult;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.JobVO;
import com.gapache.job.common.model.TriggerTaskRequest;
import com.gapache.job.server.service.JobService;
import com.gapache.security.annotation.AuthResource;
import com.gapache.security.annotation.NeedAuth;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author HuSen
 * @since 2021/2/5 4:31 下午
 */
@RestController
@NeedAuth("Job")
@RequestMapping("/api/job")
public class JobController {

    @Resource
    private JobService jobService;

    @PostMapping("/page")
    @AuthResource(scope = "page", name = "分页查询Job")
    public JsonResult<PageResult<JobVO>> page(@RequestBody IPageRequest<JobVO> iPageRequest) {
        return JsonResult.of(jobService.page(iPageRequest));
    }

    @PostMapping("/changeStatus")
    @AuthResource(scope = "changeStatus", name = "修改任务状态")
    public JsonResult<Boolean> changeStatus(@RequestBody JobVO vo) {
        return JsonResult.of(jobService.changeStatus(vo));
    }

    @PostMapping("/changeCron")
    @AuthResource(scope = "changeCron", name = "修改任务调度时间")
    public JsonResult<Boolean> changeCron(@RequestBody JobVO vo) {
        return JsonResult.of(jobService.changeCron(vo));
    }

    @PostMapping("/trigger")
    @AuthResource(scope = "trigger", name = "手动调度任务")
    public JsonResult<Boolean> trigger(@RequestBody TriggerTaskRequest request) {
        return JsonResult.of(jobService.trigger(request));
    }
}
