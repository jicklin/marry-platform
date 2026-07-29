package com.marry.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.api.dto.system.UserDTO;
import com.marry.api.query.system.UserQuery;
import com.marry.api.vo.auth.LoginVO;
import com.marry.domain.entity.SysUser;
import com.marry.common.core.domain.R;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface IUserService extends IService<SysUser> {

    SysUser getByUsername(String username);

    LoginVO.UserInfoVO getLoginUserInfo(Long userId);

    IPage<SysUser> page(UserQuery query);

    SysUser getByIdWithRoles(Long id);

    void create(UserDTO dto);

    void update(UserDTO dto);

    void remove(List<Long> ids);

    void resetPassword(Long id, String newPassword);

    void changeStatus(Long id, Integer status);
}