package com.marry.web.controller;

import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysFile;
import com.marry.system.service.IFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    @Operation(summary = "上传文件")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public R<SysFile> upload(@RequestPart("file") MultipartFile file,
                             @RequestParam(value = "dir", required = false) String dir) throws IOException {
        return R.ok(fileService.upload(file, dir));
    }

    @Operation(summary = "查询文件元数据")
    @GetMapping("/{id}")
    public R<SysFile> info(@PathVariable Long id) {
        return R.ok(fileService.getById(id));
    }
}