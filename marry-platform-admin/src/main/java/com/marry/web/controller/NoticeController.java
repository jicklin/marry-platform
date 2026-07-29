package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.common.base.PageQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysNotice;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.INoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知公告")
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final INoticeService noticeService;

    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('system:notice:list')")
    @GetMapping("/list")
    public R<IPage<SysNotice>> list(PageQuery q,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) Integer status) {
        return R.ok(noticeService.page(q, title, type, status));
    }

    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/{id}")
    public R<SysNotice> detail(@PathVariable Long id) {
        return R.ok(noticeService.getById(id));
    }

    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@RequestBody SysNotice n) {
        noticeService.create(n);
        return R.ok();
    }

    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@RequestBody SysNotice n) {
        noticeService.update(n);
        return R.ok();
    }

    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        noticeService.remove(ids);
        return R.ok();
    }
}