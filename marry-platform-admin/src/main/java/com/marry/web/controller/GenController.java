package com.marry.web.controller;

import com.marry.common.core.domain.R;
import com.marry.domain.entity.GenTable;
import com.marry.domain.entity.GenTableColumn;
import com.marry.gen.service.IGenTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "代码生成")
@RestController
@RequestMapping("/tool/gen")
@RequiredArgsConstructor
public class GenController {

    private final IGenTableService genService;

    @Operation(summary = "已导入的表")
    @PreAuthorize("hasAuthority('tool:gen:list')")
    @GetMapping("/list")
    public R<List<GenTable>> list() {
        return R.ok(genService.list());
    }

    @Operation(summary = "数据库中的所有表")
    @PreAuthorize("hasAuthority('tool:gen:list')")
    @GetMapping("/db/list")
    public R<List<GenTable>> dbList() {
        return R.ok(genService.listDbTables());
    }

    @Operation(summary = "导入表")
    @PreAuthorize("hasAuthority('tool:gen:edit')")
    @PostMapping("/importTable")
    public R<Void> importTable(@RequestBody List<String> tableNames) {
        genService.importTables(tableNames);
        return R.ok();
    }

    @Operation(summary = "同步字段")
    @PreAuthorize("hasAuthority('tool:gen:edit')")
    @PutMapping("/sync/{tableId}")
    public R<Void> sync(@PathVariable Long tableId) {
        genService.syncColumns(tableId);
        return R.ok();
    }

    @Operation(summary = "列信息")
    @PreAuthorize("hasAuthority('tool:gen:list')")
    @GetMapping("/column/{tableId}")
    public R<List<GenTableColumn>> columns(@PathVariable Long tableId) {
        return R.ok(genService.listColumns(tableId));
    }

    @Operation(summary = "下载生成的 ZIP")
    @PreAuthorize("hasAuthority('tool:gen:edit')")
    @GetMapping("/download/{tableId}")
    public void download(@PathVariable Long tableId, HttpServletResponse response) throws IOException {
        byte[] zip = genService.generateZip(tableId);
        String name = URLEncoder.encode("code.zip", StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-disposition", "attachment;filename=" + name);
        try (OutputStream out = response.getOutputStream()) {
            out.write(zip);
        }
    }
}