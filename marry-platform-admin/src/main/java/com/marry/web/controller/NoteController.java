package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.common.base.PageQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysNote;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.INoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "我的笔记")
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {

    private final INoteService noteService;

    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('note:list')")
    @GetMapping("/list")
    public R<IPage<SysNote>> list(PageQuery q,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String tag,
                                  @RequestParam(required = false) Integer status) {
        return R.ok(noteService.page(q, keyword, tag, status));
    }

    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('note:query')")
    @GetMapping("/{id}")
    public R<SysNote> detail(@PathVariable Long id) {
        return R.ok(noteService.getById(id));
    }

    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('note:add')")
    @Log(title = "笔记", businessType = BusinessType.CREATE)
    @PostMapping
    public R<Void> create(@RequestBody SysNote n) {
        noteService.create(n);
        return R.ok();
    }

    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('note:edit')")
    @Log(title = "笔记", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@RequestBody SysNote n) {
        noteService.update(n);
        return R.ok();
    }

    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('note:remove')")
    @Log(title = "笔记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        noteService.remove(ids);
        return R.ok();
    }
}
