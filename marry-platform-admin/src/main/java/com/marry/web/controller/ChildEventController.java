package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.common.base.PageQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.ChildEvent;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.IChildEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "成长记录")
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class ChildEventController {

    private final IChildEventService eventService;

    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('event:list')")
    @GetMapping("/list")
    public R<IPage<ChildEvent>> list(PageQuery q,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) String tag,
                                     @RequestParam(required = false) Integer importance,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(eventService.page(q, keyword, category, tag, importance, startDate, endDate));
    }

    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('event:query')")
    @GetMapping("/{id}")
    public R<ChildEvent> detail(@PathVariable Long id) {
        return R.ok(eventService.detail(id));
    }

    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('event:add')")
    @Log(title = "成长记录", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody ChildEvent e) {
        return R.ok(eventService.create(e));
    }

    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('event:edit')")
    @Log(title = "成长记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@RequestBody ChildEvent e) {
        eventService.update(e);
        return R.ok();
    }

    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('event:remove')")
    @Log(title = "成长记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        eventService.remove(ids);
        return R.ok();
    }

    @Operation(summary = "挂载附件")
    @PreAuthorize("hasAuthority('event:edit')")
    @Log(title = "成长记录附件", businessType = BusinessType.CREATE)
    @PostMapping("/{id}/attach")
    public R<Void> attach(@PathVariable Long id,
                          @RequestParam Long fileId,
                          @RequestParam(required = false) String mediaType) {
        eventService.attach(id, fileId, mediaType);
        return R.ok();
    }

    @Operation(summary = "移除附件")
    @PreAuthorize("hasAuthority('event:edit')")
    @Log(title = "成长记录附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}/attach/{fileId}")
    public R<Void> detach(@PathVariable Long id, @PathVariable Long fileId) {
        eventService.detach(id, fileId);
        return R.ok();
    }
}
