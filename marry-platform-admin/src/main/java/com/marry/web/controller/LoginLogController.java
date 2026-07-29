package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.api.query.monitor.LoginLogQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysLoginLog;
import com.marry.persistence.mapper.SysLoginLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "登录日志")
@RestController
@RequestMapping("/monitor/loginlog")
@RequiredArgsConstructor
public class LoginLogController {

    private final SysLoginLogMapper loginLogMapper;

    @Operation(summary = "登录日志列表")
    @PreAuthorize("hasAuthority('monitor:loginlog:list')")
    @GetMapping("/list")
    public R<IPage<SysLoginLog>> list(LoginLogQuery query) {
        return R.ok(loginLogMapper.selectPage(query.toPage(), null));
    }

    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('monitor:loginlog:remove')")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        for (Long id : ids) loginLogMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "清空")
    @PreAuthorize("hasAuthority('monitor:loginlog:remove')")
    @DeleteMapping("/clean")
    public R<Void> clean() {
        loginLogMapper.cleanBefore(LocalDateTime.now());
        return R.ok();
    }
}