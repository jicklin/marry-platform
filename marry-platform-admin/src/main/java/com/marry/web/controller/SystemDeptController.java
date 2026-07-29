package com.marry.web.controller;

import com.marry.api.dto.system.DeptDTO;
import com.marry.api.vo.system.DeptTreeVO;
import com.marry.common.core.domain.R;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.IDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SystemDeptController {

    private final IDeptService deptService;

    @Operation(summary = "部门树")
    @PreAuthorize("hasAuthority('system:dept:list')")
    @GetMapping("/tree")
    public R<List<DeptTreeVO>> tree(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) Integer status) {
        return R.ok(deptService.tree(name, status));
    }

    @Operation(summary = "新增部门")
    @PreAuthorize("hasAuthority('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@Valid @RequestBody DeptDTO dto) {
        deptService.create(dto);
        return R.ok();
    }

    @Operation(summary = "修改部门")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody DeptDTO dto) {
        deptService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除部门")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        deptService.remove(id);
        return R.ok();
    }
}