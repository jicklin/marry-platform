package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.api.dto.system.DictDataDTO;
import com.marry.api.dto.system.DictTypeDTO;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysDictData;
import com.marry.domain.entity.SysDictType;
import com.marry.persistence.mapper.SysDictDataMapper;
import com.marry.persistence.mapper.SysDictTypeMapper;
import com.marry.system.service.IDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements IDictService {

    private final SysDictDataMapper dictDataMapper;

    @Override
    public IPage<SysDictType> pageType(PageQuery query, String name, String type, Integer status) {
        LambdaQueryWrapper<SysDictType> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) q.like(SysDictType::getName, name);
        if (StrUtil.isNotBlank(type)) q.like(SysDictType::getType, type);
        if (status != null) q.eq(SysDictType::getStatus, status);
        q.orderByDesc(SysDictType::getCreateTime);
        return baseMapper.selectPage(query.toPage(), q);
    }

    @Override
    @Transactional
    public void createType(DictTypeDTO dto) {
        SysDictType t = new SysDictType();
        BeanUtils.copyProperties(dto, t);
        if (t.getStatus() == null) t.setStatus(1);
        baseMapper.insert(t);
    }

    @Override
    @Transactional
    public void updateType(DictTypeDTO dto) {
        if (dto.getId() == null) return;
        SysDictType t = new SysDictType();
        BeanUtils.copyProperties(dto, t);
        baseMapper.updateById(t);
    }

    @Override
    @Transactional
    public void removeType(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) {
            baseMapper.deleteById(id);
            dictDataMapper.delete(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType,
                    baseMapper.selectById(id) == null ? "" : baseMapper.selectById(id).getType()));
        }
    }

    @Override
    public IPage<SysDictData> pageData(PageQuery query, String dictType, String label, Integer status) {
        LambdaQueryWrapper<SysDictData> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(dictType)) q.eq(SysDictData::getDictType, dictType);
        if (StrUtil.isNotBlank(label)) q.like(SysDictData::getLabel, label);
        if (status != null) q.eq(SysDictData::getStatus, status);
        q.orderByAsc(SysDictData::getOrderNum);
        return dictDataMapper.selectPage(query.toPage(), q);
    }

    @Override
    @Transactional
    public void createData(DictDataDTO dto) {
        SysDictData d = new SysDictData();
        BeanUtils.copyProperties(dto, d);
        if (d.getStatus() == null) d.setStatus(1);
        if (d.getOrderNum() == null) d.setOrderNum(0);
        dictDataMapper.insert(d);
    }

    @Override
    @Transactional
    public void updateData(DictDataDTO dto) {
        if (dto.getId() == null) return;
        SysDictData d = new SysDictData();
        BeanUtils.copyProperties(dto, d);
        dictDataMapper.updateById(d);
    }

    @Override
    @Transactional
    public void removeData(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) dictDataMapper.deleteById(id);
    }

    @Override
    public List<SysDictData> listByDictType(String dictType) {
        return dictDataMapper.selectByDictType(dictType);
    }
}