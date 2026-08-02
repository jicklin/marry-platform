package com.marry.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.ChildEvent;

import java.time.LocalDate;
import java.util.List;

public interface IChildEventService extends IService<ChildEvent> {

    IPage<ChildEvent> page(PageQuery q, String keyword, String category, String tag,
                           Integer importance, LocalDate startDate, LocalDate endDate);

    ChildEvent detail(Long id);

    Long create(ChildEvent e);

    void update(ChildEvent e);

    void remove(List<Long> ids);

    void attach(Long eventId, Long fileId, String mediaType);

    void detach(Long eventId, Long fileId);
}
