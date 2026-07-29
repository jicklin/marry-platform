package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysNotice;
import com.marry.persistence.mapper.SysNoticeMapper;
import com.marry.system.service.INoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements INoticeService {

    @Override
    public IPage<SysNotice> page(PageQuery q, String title, String type, Integer status) {
        LambdaQueryWrapper<SysNotice> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(title)) w.like(SysNotice::getTitle, title);
        if (StrUtil.isNotBlank(type)) w.eq(SysNotice::getType, type);
        if (status != null) w.eq(SysNotice::getStatus, status);
        w.orderByDesc(SysNotice::getCreateTime);
        return baseMapper.selectPage(q.toPage(), w);
    }

    @Override
    @Transactional
    public void create(SysNotice n) {
        if (n.getStatus() == null) n.setStatus(1);
        baseMapper.insert(n);
    }

    @Override
    @Transactional
    public void update(SysNotice n) {
        if (n.getId() == null) return;
        baseMapper.updateById(n);
    }

    @Override
    @Transactional
    public void remove(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) baseMapper.deleteById(id);
    }
}