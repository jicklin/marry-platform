package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.api.dto.system.ConfigDTO;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysConfig;
import com.marry.persistence.mapper.SysConfigMapper;
import com.marry.system.service.IConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements IConfigService {

    @Override
    public IPage<SysConfig> page(PageQuery query, String name, String configKey, Integer configType) {
        LambdaQueryWrapper<SysConfig> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) q.like(SysConfig::getName, name);
        if (StrUtil.isNotBlank(configKey)) q.like(SysConfig::getConfigKey, configKey);
        if (configType != null) q.eq(SysConfig::getConfigType, configType);
        q.orderByDesc(SysConfig::getCreateTime);
        return baseMapper.selectPage(query.toPage(), q);
    }

    @Override
    @Transactional
    public void create(ConfigDTO dto) {
        SysConfig c = new SysConfig();
        BeanUtils.copyProperties(dto, c);
        if (c.getConfigType() == null) c.setConfigType(2);
        if (c.getIsBuiltin() == null) c.setIsBuiltin(0);
        baseMapper.insert(c);
    }

    @Override
    @Transactional
    public void update(ConfigDTO dto) {
        if (dto.getId() == null) return;
        SysConfig c = new SysConfig();
        BeanUtils.copyProperties(dto, c);
        baseMapper.updateById(c);
    }

    @Override
    @Transactional
    public void remove(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) baseMapper.deleteById(id);
    }

    @Override
    public String getConfigValueByKey(String key) {
        if (StrUtil.isBlank(key)) return null;
        SysConfig c = baseMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return c == null ? null : c.getConfigValue();
    }
}