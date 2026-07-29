package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.api.dto.system.DeptDTO;
import com.marry.api.vo.system.DeptTreeVO;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.domain.entity.SysDept;
import com.marry.persistence.mapper.SysDeptMapper;
import com.marry.system.service.IDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements IDeptService {

    @Override
    public List<DeptTreeVO> tree(String name, Integer status) {
        LambdaQueryWrapper<SysDept> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) q.like(SysDept::getName, name);
        if (status != null) q.eq(SysDept::getStatus, status);
        q.orderByAsc(SysDept::getOrderNum);
        List<SysDept> all = baseMapper.selectList(q);
        return buildTree(all);
    }

    private List<DeptTreeVO> buildTree(List<SysDept> all) {
        Map<Long, DeptTreeVO> map = new LinkedHashMap<>();
        for (SysDept d : all) {
            DeptTreeVO vo = new DeptTreeVO();
            BeanUtils.copyProperties(d, vo);
            map.put(d.getId(), vo);
        }
        List<DeptTreeVO> roots = new ArrayList<>();
        for (SysDept d : all) {
            DeptTreeVO vo = map.get(d.getId());
            Long pid = d.getParentId() == null ? 0L : d.getParentId();
            if (pid == 0L || !map.containsKey(pid)) roots.add(vo);
            else map.get(pid).getChildren().add(vo);
        }
        return roots;
    }

    @Override
    @Transactional
    public void create(DeptDTO dto) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        if (dept.getParentId() == null) dept.setParentId(0L);
        if (dept.getStatus() == null) dept.setStatus(1);
        if (dept.getOrderNum() == null) dept.setOrderNum(0);
        // ancestors: parent.ancestors + parent.id + "/"
        if (dept.getParentId() != 0L) {
            SysDept parent = baseMapper.selectById(dept.getParentId());
            String pa = parent == null || StrUtil.isBlank(parent.getAncestors()) ? "0/" : parent.getAncestors();
            dept.setAncestors(pa + dept.getParentId() + "/");
        } else {
            dept.setAncestors("0/");
        }
        baseMapper.insert(dept);
    }

    @Override
    @Transactional
    public void update(DeptDTO dto) {
        if (dto.getId() == null) throw new BizException(BizCode.BAD_REQUEST, "id 不能为空");
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        baseMapper.updateById(dept);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        if (id == null) return;
        Long childCount = baseMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException(BizCode.BAD_REQUEST, "存在下级部门，不允许删除");
        }
        baseMapper.deleteById(id);
    }
}