package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysNote;
import com.marry.persistence.mapper.SysNoteMapper;
import com.marry.system.service.INoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<SysNoteMapper, SysNote> implements INoteService {

    @Override
    public IPage<SysNote> page(PageQuery q, String keyword, String tag, Integer status) {
        LambdaQueryWrapper<SysNote> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            w.and(x -> x.like(SysNote::getTitle, keyword).or().like(SysNote::getContent, keyword));
        }
        if (StrUtil.isNotBlank(tag)) w.like(SysNote::getTags, tag);
        if (status != null) w.eq(SysNote::getStatus, status);
        // pinned first, then newest updates
        w.orderByDesc(SysNote::getIsPinned).orderByDesc(SysNote::getUpdateTime);
        return baseMapper.selectPage(q.toPage(), w);
    }

    @Override
    @Transactional
    public void create(SysNote n) {
        if (n.getStatus() == null) n.setStatus(1);
        if (n.getIsPinned() == null) n.setIsPinned(0);
        baseMapper.insert(n);
    }

    @Override
    @Transactional
    public void update(SysNote n) {
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
