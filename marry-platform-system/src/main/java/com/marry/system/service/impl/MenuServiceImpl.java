package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.api.dto.system.MenuDTO;
import com.marry.api.vo.system.MenuTreeVO;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.domain.entity.SysMenu;
import com.marry.persistence.mapper.SysMenuMapper;
import com.marry.persistence.mapper.SysRoleMenuMapper;
import com.marry.system.service.IMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements IMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuTreeVO> buildMenuTree(List<SysMenu> menus) {
        if (CollUtil.isEmpty(menus)) return List.of();
        // Map id -> VO
        Map<Long, MenuTreeVO> map = new LinkedHashMap<>();
        for (SysMenu m : menus) {
            MenuTreeVO vo = new MenuTreeVO();
            BeanUtils.copyProperties(m, vo);
            vo.setMeta(new MenuTreeVO.Meta());
            vo.getMeta().setTitle(m.getName());
            vo.getMeta().setIcon(m.getIcon());
            vo.getMeta().setHidden(m.getVisible() != null && m.getVisible() == 0);
            vo.getMeta().setKeepAlive(m.getIsCache() != null && m.getIsCache() == 1);
            if (StrUtil.isNotBlank(m.getPerm())) vo.getMeta().setPerms(List.of(m.getPerm()));
            map.put(m.getId(), vo);
        }
        List<MenuTreeVO> roots = new ArrayList<>();
        for (SysMenu m : menus) {
            MenuTreeVO vo = map.get(m.getId());
            Long pid = m.getParentId() == null ? 0L : m.getParentId();
            if (pid == null || pid == 0L || !map.containsKey(pid)) {
                roots.add(vo);
            } else {
                map.get(pid).getChildren().add(vo);
            }
        }
        return roots;
    }

    @Override
    public List<MenuTreeVO> listAllAsTree() {
        LambdaQueryWrapper<SysMenu> q = new LambdaQueryWrapper<>();
        q.orderByAsc(SysMenu::getOrderNum);
        return buildMenuTree(baseMapper.selectList(q));
    }

    @Override
    public List<MenuTreeVO> listForUser(Long userId) {
        List<SysMenu> menus = baseMapper.selectByUserId(userId);
        // Filter: only menus (M/C), not buttons (F), and visible
        menus = menus.stream()
                .filter(m -> !"F".equals(m.getMenuType()))
                .filter(m -> m.getVisible() == null || m.getVisible() == 1)
                .filter(m -> m.getStatus() == null || m.getStatus() == 1)
                .collect(Collectors.toList());
        return buildMenuTree(menus);
    }

    @Override
    public List<MenuTreeVO> listRoleMenuTree(Long roleId) {
        // Return all menus; controller will mark checked ones via separate API
        return listAllAsTree();
    }

    @Override
    public List<String> permsByUserId(Long userId) {
        return baseMapper.selectPermsByUserId(userId);
    }

    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void create(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getOrderNum() == null) menu.setOrderNum(0);
        if (menu.getVisible() == null) menu.setVisible(1);
        if (menu.getStatus() == null) menu.setStatus(1);
        baseMapper.insert(menu);
    }

    @Override
    @Transactional
    public void update(MenuDTO dto) {
        if (dto.getId() == null) throw new BizException(BizCode.BAD_REQUEST, "id 不能为空");
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        baseMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        if (id == null) return;
        SysMenu menu = baseMapper.selectById(id);
        if (menu == null) return;
        // Check if any child
        Long childCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException(BizCode.BAD_REQUEST, "存在子菜单，不允许删除");
        }
        roleMenuMapper.deleteByMenuId(id);
        baseMapper.deleteById(id);
    }
}