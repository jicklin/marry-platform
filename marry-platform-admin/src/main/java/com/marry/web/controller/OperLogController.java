package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.api.query.monitor.OperLogQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysOperLog;
import com.marry.persistence.mapper.SysOperLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/monitor/operlog")
@RequiredArgsConstructor
public class OperLogController {

    private final SysOperLogMapper operLogMapper;

    @Operation(summary = "操作日志列表")
    @PreAuthorize("hasAuthority('monitor:operlog:list')")
    @GetMapping("/list")
    public R<IPage<SysOperLog>> list(OperLogQuery query) {
        return R.ok(operLogMapper.selectPage(query.toPage(), null));
    }

    @Operation(summary = "删除日志")
    @PreAuthorize("hasAuthority('monitor:operlog:remove')")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        for (Long id : ids) operLogMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "清空日志")
    @PreAuthorize("hasAuthority('monitor:operlog:remove')")
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogMapper.cleanBefore(LocalDateTime.now());
        return R.ok();
    }
}