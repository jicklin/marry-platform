package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.api.dto.system.ConfigDTO;
import com.marry.domain.entity.SysConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.marry.common.base.PageQuery;

import java.util.List;

public interface IConfigService extends IService<SysConfig> {

    IPage<SysConfig> page(PageQuery query, String name, String configKey, Integer configType);

    void create(ConfigDTO dto);

    void update(ConfigDTO dto);

    void remove(List<Long> ids);

    String getConfigValueByKey(String key);
}