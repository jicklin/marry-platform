package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.api.dto.system.RoleDTO;
import com.marry.api.query.system.RoleQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysRole;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SystemRoleController {

    private final IRoleService roleService;

    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/list")
    public R<IPage<SysRole>> list(RoleQuery query) {
        return R.ok(roleService.page(query));
    }

    @Operation(summary = "所有启用的角色")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.ok(roleService.listAll());
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@Valid @RequestBody RoleDTO dto) {
        roleService.create(dto);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody RoleDTO dto) {
        roleService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        roleService.remove(ids);
        return R.ok();
    }

    @Operation(summary = "修改角色状态")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        roleService.changeStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "修改数据权限")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public R<Void> dataScope(@RequestParam Long id,
                             @RequestParam Integer dataScope,
                             @RequestParam(required = false) List<Long> deptIds) {
        roleService.changeDataScope(id, dataScope, deptIds);
        return R.ok();
    }

    @Operation(summary = "已分配该角色的用户列表")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/authUser/{roleId}")
    public R<Map<String, Object>> authUserList(@PathVariable Long roleId,
                                              @RequestParam(required = false) String username,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(roleService.authUsers(roleId, username, pageNum, pageSize));
    }

    @Operation(summary = "批量分配用户到角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/assign")
    public R<Void> assignUsers(@RequestParam Long roleId, @RequestBody List<Long> userIds) {
        roleService.authUsersAssign(roleId, userIds);
        return R.ok();
    }
}