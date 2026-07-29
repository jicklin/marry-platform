package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.api.dto.system.MenuDTO;
import com.marry.api.dto.system.RoleDTO;
import com.marry.api.vo.system.MenuTreeVO;
import com.marry.domain.entity.SysMenu;

import java.util.List;

public interface IMenuService extends IService<SysMenu> {

    List<MenuTreeVO> buildMenuTree(List<SysMenu> menus);

    List<MenuTreeVO> listAllAsTree();

    List<MenuTreeVO> listForUser(Long userId);

    List<MenuTreeVO> listRoleMenuTree(Long roleId);

    List<String> permsByUserId(Long userId);

    List<Long> listMenuIdsByRoleId(Long roleId);

    void create(MenuDTO dto);

    void update(MenuDTO dto);

    void remove(Long id);
}