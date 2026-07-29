package com.marry.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marry.domain.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    int deleteByRoleId(@Param("roleId") Long roleId);

    int deleteByMenuId(@Param("menuId") Long menuId);
}