package com.marry.system.service.impl;

import com.marry.api.vo.system.MenuTreeVO;
import com.marry.domain.entity.SysMenu;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceImplTest {

    @Test
    void buildMenuTree_nestsChildrenByParentId() {
        SysMenu parent = new SysMenu();
        parent.setId(1L);
        parent.setName("系统管理");
        parent.setMenuType("M");
        parent.setPath("system");

        SysMenu child = new SysMenu();
        child.setId(2L);
        child.setParentId(1L);
        child.setName("用户管理");
        child.setMenuType("C");
        child.setPath("user");

        SysMenu button = new SysMenu();
        button.setId(3L);
        button.setParentId(2L);
        button.setName("新增按钮");
        button.setMenuType("F");
        button.setPerm("system:user:add");

        MenuServiceImpl svc = new MenuServiceImpl(null);
        List<MenuTreeVO> tree = svc.buildMenuTree(List.of(parent, child, button));

        assertEquals(1, tree.size());
        MenuTreeVO root = tree.get(0);
        assertEquals("系统管理", root.getName());
        assertEquals(1, root.getChildren().size());
        assertEquals("用户管理", root.getChildren().get(0).getName());
        assertEquals(1, root.getChildren().get(0).getChildren().size());
        assertEquals("新增按钮", root.getChildren().get(0).getChildren().get(0).getName());
    }

    @Test
    void buildMenuTree_emptyList_returnsEmpty() {
        MenuServiceImpl svc = new MenuServiceImpl(null);
        assertTrue(svc.buildMenuTree(List.of()).isEmpty());
    }
}