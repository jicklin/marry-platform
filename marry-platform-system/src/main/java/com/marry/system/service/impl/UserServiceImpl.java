package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.api.dto.system.UserDTO;
import com.marry.api.query.system.UserQuery;
import com.marry.api.vo.auth.LoginVO;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.domain.entity.*;
import com.marry.persistence.mapper.*;
import com.marry.system.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements IUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        if (StrUtil.isBlank(username)) return null;
        return userMapper.selectByUsername(username);
    }

    @Override
    public LoginVO.UserInfoVO getLoginUserInfo(Long userId) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BizException(BizCode.USER_NOT_FOUND);
        }
        LoginVO.UserInfoVO vo = new LoginVO.UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickName(user.getNickName());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setSex(user.getSex());
        vo.setDeptId(user.getDeptId());
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) vo.setDeptName(dept.getName());
        }
        vo.setPermissions(userMapper.selectPermsByUserId(userId));
        vo.setRoles(userMapper.selectRoleCodesByUserId(userId));
        return vo;
    }

    @Override
    public IPage<SysUser> page(UserQuery query) {
        LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getUsername())) q.like(SysUser::getUsername, query.getUsername());
        if (StrUtil.isNotBlank(query.getNickName())) q.like(SysUser::getNickName, query.getNickName());
        if (StrUtil.isNotBlank(query.getPhone())) q.like(SysUser::getPhone, query.getPhone());
        if (query.getDeptId() != null) q.eq(SysUser::getDeptId, query.getDeptId());
        if (query.getStatus() != null) q.eq(SysUser::getStatus, query.getStatus());
        q.orderByDesc(SysUser::getCreateTime);
        return baseMapper.selectPage(query.toPage(), q);
    }

    @Override
    public SysUser getByIdWithRoles(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) return null;
        user.setRoleIds(roleIdsOf(id));
        return user;
    }

    @Override
    @Transactional
    public void create(UserDTO dto) {
        if (StrUtil.isNotBlank(dto.getUsername()) && getByUsername(dto.getUsername()) != null) {
            throw new BizException(BizCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        copyProps(user, dto);
        if (StrUtil.isBlank(dto.getPassword())) {
            throw new BizException(BizCode.BAD_REQUEST, "密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        userMapper.insert(user);

        if (CollUtil.isNotEmpty(dto.getRoleIds())) {
            for (Long rid : dto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(rid);
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional
    public void update(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "id 不能为空");
        }
        SysUser existing = baseMapper.selectById(dto.getId());
        if (existing == null) throw new BizException(BizCode.USER_NOT_FOUND);

        SysUser user = new SysUser();
        user.setId(dto.getId());
        copyProps(user, dto);
        user.setPassword(null); // never overwrite password here
        baseMapper.updateById(user);

        if (dto.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(dto.getId());
            for (Long rid : dto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(dto.getId());
                ur.setRoleId(rid);
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional
    public void remove(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) {
            baseMapper.deleteById(id);
            userRoleMapper.deleteByUserId(id);
        }
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) throw new BizException(BizCode.USER_NOT_FOUND);
        user.setPassword(passwordEncoder.encode(newPassword));
        baseMapper.updateById(user);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) throw new BizException(BizCode.USER_NOT_FOUND);
        user.setStatus(status);
        baseMapper.updateById(user);
    }

    private void copyProps(SysUser user, UserDTO dto) {
        user.setUsername(dto.getUsername());
        user.setNickName(dto.getNickName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setSex(dto.getSex());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());
        user.setRemark(dto.getRemark());
    }

    public List<Long> roleIdsOf(Long userId) {
        if (userId == null) return List.of();
        return userMapper.selectRoleIdsByUserId(userId).stream().distinct().collect(Collectors.toList());
    }
}