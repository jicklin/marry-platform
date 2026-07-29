package com.marry.web.controller;

import com.marry.api.dto.system.MenuDTO;
import com.marry.api.vo.system.MenuTreeVO;
import com.marry.common.core.domain.R;
import com.marry.common.core.exception.BizException;
import com.marry.common.core.domain.BizCode;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.security.util.SecurityUtil;
import com.marry.system.service.IMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SystemMenuController {

    private final IMenuService menuService;

    @Operation(summary = "全部菜单树")
    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/tree")
    public R<List<MenuTreeVO>> tree() {
        return R.ok(menuService.listAllAsTree());
    }

    @Operation(summary = "当前用户路由（动态路由）")
    @GetMapping("/routers")
    public R<List<MenuTreeVO>> routers() {
        Long uid = SecurityUtil.currentUserId();
        if (uid == null) throw new BizException(BizCode.UNAUTHORIZED);
        return R.ok(menuService.listForUser(uid));
    }

    @Operation(summary = "角色的菜单树（含checked标记）")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public R<Object> roleMenuTreeselect(@PathVariable Long roleId) {
        List<MenuTreeVO> menus = menuService.listRoleMenuTree(roleId);
        List<Long> checked = menuService.listMenuIdsByRoleId(roleId);
        return R.ok(java.util.Map.of("menus", menus, "checkedKeys", checked));
    }

    @Operation(summary = "新增菜单")
    @PreAuthorize("hasAuthority('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@Valid @RequestBody MenuDTO dto) {
        menuService.create(dto);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody MenuDTO dto) {
        menuService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        menuService.remove(id);
        return R.ok();
    }
}