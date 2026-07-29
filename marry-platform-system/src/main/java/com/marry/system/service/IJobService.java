package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysJob;
import com.marry.domain.entity.SysJobLog;

public interface IJobService extends IService<SysJob> {

    com.baomidou.mybatisplus.core.metadata.IPage<SysJob> page(PageQuery q, String name, Integer status);

    com.baomidou.mybatisplus.core.metadata.IPage<SysJobLog> logPage(PageQuery q, Long jobId, Integer status);

    void create(SysJob job);

    void update(SysJob job);

    void remove(java.util.List<Long> ids);

    void changeStatus(Long id, Integer status);

    /** Run the job once, immediately (not scheduled). */
    void runOnce(Long id);
}