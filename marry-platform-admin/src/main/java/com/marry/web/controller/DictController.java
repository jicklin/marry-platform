package com.marry.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.api.dto.system.DictDataDTO;
import com.marry.api.dto.system.DictTypeDTO;
import com.marry.common.base.PageQuery;
import com.marry.common.core.domain.R;
import com.marry.domain.entity.SysDictData;
import com.marry.domain.entity.SysDictType;
import com.marry.log.annotation.Log;
import com.marry.log.enums.BusinessType;
import com.marry.system.service.IDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final IDictService dictService;

    @Operation(summary = "字典类型分页")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/type/list")
    public R<IPage<SysDictType>> typeList(PageQuery query,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) Integer status) {
        return R.ok(dictService.pageType(query, name, type, status));
    }

    @Operation(summary = "新增字典类型")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典类型", businessType = BusinessType.CREATE)
    @PostMapping("/type")
    public R<Void> addType(@Valid @RequestBody DictTypeDTO dto) {
        dictService.createType(dto);
        return R.ok();
    }

    @Operation(summary = "修改字典类型")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping("/type")
    public R<Void> updateType(@Valid @RequestBody DictTypeDTO dto) {
        dictService.updateType(dto);
        return R.ok();
    }

    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/type/{ids}")
    public R<Void> removeType(@PathVariable List<Long> ids) {
        dictService.removeType(ids);
        return R.ok();
    }

    @Operation(summary = "字典数据分页")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/data/list")
    public R<IPage<SysDictData>> dataList(PageQuery query,
                                          @RequestParam(required = false) String dictType,
                                          @RequestParam(required = false) String label,
                                          @RequestParam(required = false) Integer status) {
        return R.ok(dictService.pageData(query, dictType, label, status));
    }

    @Operation(summary = "按字典类型查询数据")
    @GetMapping("/data/type")
    public R<List<SysDictData>> dataByType(@RequestParam String dictType) {
        return R.ok(dictService.listByDictType(dictType));
    }

    @Operation(summary = "新增字典数据")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典数据", businessType = BusinessType.CREATE)
    @PostMapping("/data")
    public R<Void> addData(@Valid @RequestBody DictDataDTO dto) {
        dictService.createData(dto);
        return R.ok();
    }

    @Operation(summary = "修改字典数据")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping("/data")
    public R<Void> updateData(@Valid @RequestBody DictDataDTO dto) {
        dictService.updateData(dto);
        return R.ok();
    }

    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{ids}")
    public R<Void> removeData(@PathVariable List<Long> ids) {
        dictService.removeData(ids);
        return R.ok();
    }
}