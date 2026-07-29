package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.api.dto.system.UserDTO;
import com.marry.api.query.system.UserQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysUser;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SystemUserController {

    private final IUserService userService;

    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/list")
    public R<IPage<SysUser>> list(UserQuery query) {
        return R.ok(userService.page(query));
    }

    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{id}")
    public R<SysUser> detail(@PathVariable Long id) {
        return R.ok(userService.getByIdWithRoles(id));
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@Valid @RequestBody UserDTO dto) {
        userService.create(dto);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody UserDTO dto) {
        userService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        userService.remove(ids);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd/{id}")
    public R<Void> resetPwd(@PathVariable Long id, @RequestParam String password) {
        userService.resetPassword(id, password);
        return R.ok();
    }

    @Operation(summary = "修改状态")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{id}")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return R.ok();
    }
}