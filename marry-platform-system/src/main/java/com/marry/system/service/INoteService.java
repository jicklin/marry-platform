package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysNote;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface INoteService extends IService<SysNote> {

    IPage<SysNote> page(PageQuery q, String keyword, String tag, Integer status);

    void create(SysNote n);

    void update(SysNote n);

    void remove(List<Long> ids);
}
