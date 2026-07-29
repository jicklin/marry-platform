package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.api.dto.system.DictDataDTO;
import com.marry.api.dto.system.DictTypeDTO;
import com.marry.domain.entity.SysDictData;
import com.marry.domain.entity.SysDictType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.common.base.PageQuery;

import java.util.List;

public interface IDictService extends IService<SysDictType> {

    IPage<SysDictType> pageType(PageQuery query, String name, String type, Integer status);

    void createType(DictTypeDTO dto);

    void updateType(DictTypeDTO dto);

    void removeType(List<Long> ids);

    IPage<SysDictData> pageData(PageQuery query, String dictType, String label, Integer status);

    void createData(DictDataDTO dto);

    void updateData(DictDataDTO dto);

    void removeData(List<Long> ids);

    List<SysDictData> listByDictType(String dictType);
}