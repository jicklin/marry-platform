package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.common.base.PageQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysJob;
import com.marry.domain.entity.SysJobLog;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.IJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "定时任务")
@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class JobController {

    private final IJobService jobService;

    @Operation(summary = "任务分页")
    @PreAuthorize("hasAuthority('monitor:job:list')")
    @GetMapping("/list")
    public R<IPage<SysJob>> list(PageQuery q,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(jobService.page(q, name, status));
    }

    @Operation(summary = "新增任务")
    @PreAuthorize("hasAuthority('monitor:job:add')")
    @Log(title = "定时任务", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@RequestBody SysJob job) {
        jobService.create(job);
        return R.ok();
    }

    @Operation(summary = "修改任务")
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@RequestBody SysJob job) {
        jobService.update(job);
        return R.ok();
    }

    @Operation(summary = "删除任务")
    @PreAuthorize("hasAuthority('monitor:job:remove')")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        jobService.remove(ids);
        return R.ok();
    }

    @Operation(summary = "启停")
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        jobService.changeStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "立即执行一次")
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/run/{id}")
    public R<Void> runOnce(@PathVariable Long id) {
        jobService.runOnce(id);
        return R.ok();
    }

    @Operation(summary = "执行日志")
    @PreAuthorize("hasAuthority('monitor:job:list')")
    @GetMapping("/log")
    public R<IPage<SysJobLog>> logPage(PageQuery q,
                                       @RequestParam(required = false) Long jobId,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(jobService.logPage(q, jobId, status));
    }
}