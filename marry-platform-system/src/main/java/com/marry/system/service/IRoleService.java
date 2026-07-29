package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.api.dto.system.RoleDTO;
import com.marry.api.query.system.RoleQuery;
import com.marry.domain.entity.SysRole;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface IRoleService extends IService<SysRole> {

    IPage<SysRole> page(RoleQuery query);

    List<SysRole> listAll();

    void create(RoleDTO dto);

    void update(RoleDTO dto);

    void remove(List<Long> ids);

    void changeStatus(Long id, Integer status);

    void changeDataScope(Long id, Integer dataScope, List<Long> deptIds);

    /** Authorized users under this role. */
    java.util.Map<String, Object> authUsers(Long roleId, String username, int pageNum, int pageSize);

    void authUsersAssign(Long roleId, List<Long> userIds);
}