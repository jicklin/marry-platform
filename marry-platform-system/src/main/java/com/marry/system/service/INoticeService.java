package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysNotice;

public interface INoticeService extends IService<SysNotice> {

    com.baomidou.mybatisplus.core.metadata.IPage<SysNotice> page(PageQuery q, String title, String type, Integer status);

    void create(SysNotice n);

    void update(SysNotice n);

    void remove(java.util.List<Long> ids);
}