package com.marry.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marry.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<SysMenu> selectByUserId(@Param("userId") Long userId);

    List<String> selectPermsByUserId(@Param("userId") Long userId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}