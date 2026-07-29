package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.api.dto.system.ConfigDTO;
import com.marry.common.base.PageQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysConfig;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.IConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "参数配置")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class ConfigController {

    private final IConfigService configService;

    @Operation(summary = "分页查询")
    @PreAuthorize("hasAuthority('system:config:list')")
    @GetMapping("/list")
    public R<IPage<SysConfig>> list(PageQuery query,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String configKey,
                                    @RequestParam(required = false) Integer configType) {
        return R.ok(configService.page(query, name, configKey, configType));
    }

    @Operation(summary = "按 key 获取配置值")
    @GetMapping("/key")
    public R<String> byKey(@RequestParam String key) {
        return R.ok(configService.getConfigValueByKey(key));
    }

    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('system:config:add')")
    @Log(title = "参数配置", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@Valid @RequestBody ConfigDTO dto) {
        configService.create(dto);
        return R.ok();
    }

    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @Log(title = "参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody ConfigDTO dto) {
        configService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('system:config:remove')")
    @Log(title = "参数配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        configService.remove(ids);
        return R.ok();
    }
}