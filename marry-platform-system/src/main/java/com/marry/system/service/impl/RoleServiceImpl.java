package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.api.dto.system.RoleDTO;
import com.marry.api.query.system.RoleQuery;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.domain.entity.*;
import com.marry.persistence.mapper.*;
import com.marry.system.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements IRoleService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserMapper userMapper;

    @Override
    public IPage<SysRole> page(RoleQuery query) {
        LambdaQueryWrapper<SysRole> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getName())) q.like(SysRole::getName, query.getName());
        if (StrUtil.isNotBlank(query.getCode())) q.like(SysRole::getCode, query.getCode());
        if (query.getStatus() != null) q.eq(SysRole::getStatus, query.getStatus());
        q.orderByAsc(SysRole::getId);
        return baseMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), q);
    }

    @Override
    public List<SysRole> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getId));
    }

    @Override
    @Transactional
    public void create(RoleDTO dto) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getDataScope() == null) role.setDataScope(1);
        baseMapper.insert(role);
        applyMenus(role.getId(), dto.getMenuIds());
    }

    @Override
    @Transactional
    public void update(RoleDTO dto) {
        if (dto.getId() == null) throw new BizException(BizCode.BAD_REQUEST, "id 不能为空");
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        baseMapper.updateById(role);
        if (dto.getMenuIds() != null) {
            roleMenuMapper.deleteByRoleId(role.getId());
            applyMenus(role.getId(), dto.getMenuIds());
        }
    }

    @Override
    @Transactional
    public void remove(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) {
            // Ensure no users assigned
            Long cnt = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
            if (cnt != null && cnt > 0) {
                throw new BizException(BizCode.ROLE_HAS_USERS);
            }
            baseMapper.deleteById(id);
            roleMenuMapper.deleteByRoleId(id);
        }
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysRole role = baseMapper.selectById(id);
        if (role == null) throw new BizException(BizCode.NOT_FOUND, "角色不存在");
        role.setStatus(status);
        baseMapper.updateById(role);
    }

    @Override
    @Transactional
    public void changeDataScope(Long id, Integer dataScope, List<Long> deptIds) {
        SysRole role = baseMapper.selectById(id);
        if (role == null) throw new BizException(BizCode.NOT_FOUND, "角色不存在");
        role.setDataScope(dataScope);
        baseMapper.updateById(role);
        // (sys_role_dept management is not implemented here; can be extended)
    }

    @Override
    public Map<String, Object> authUsers(Long roleId, String username, int pageNum, int pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<>();
        q.inSql(SysUser::getId,
                "SELECT user_id FROM sys_user_role WHERE role_id = " + roleId);
        if (StrUtil.isNotBlank(username)) q.like(SysUser::getUsername, username);
        IPage<SysUser> result = userMapper.selectPage(page, q);
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("records", result.getRecords());
        ret.put("total", result.getTotal());
        return ret;
    }

    @Override
    @Transactional
    public void authUsersAssign(Long roleId, List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) return;
        for (Long uid : userIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(uid);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    private void applyMenus(Long roleId, List<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds) || roleId == null) return;
        // expand menus to include all parents
        List<SysMenu> all = menuMapper.selectList(null);
        Map<Long, SysMenu> map = new HashMap<>();
        for (SysMenu m : all) map.put(m.getId(), m);
        Set<Long> result = new HashSet<>(menuIds);
        for (Long mid : new ArrayList<>(menuIds)) {
            SysMenu m = map.get(mid);
            while (m != null && m.getParentId() != null && m.getParentId() != 0L) {
                result.add(m.getParentId());
                m = map.get(m.getParentId());
            }
        }
        for (Long mid : result) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(mid);
            roleMenuMapper.insert(rm);
        }
    }
}