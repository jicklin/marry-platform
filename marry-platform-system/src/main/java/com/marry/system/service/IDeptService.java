package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.api.dto.system.DeptDTO;
import com.marry.api.vo.system.DeptTreeVO;
import com.marry.domain.entity.SysDept;

import java.util.List;

public interface IDeptService extends IService<SysDept> {

    List<DeptTreeVO> tree(String name, Integer status);

    void create(DeptDTO dto);

    void update(DeptDTO dto);

    void remove(Long id);
}