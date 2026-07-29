package com.marry.system.service.impl;

import com.marry.api.dto.system.UserDTO;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.domain.entity.SysUser;
import com.marry.persistence.mapper.SysDeptMapper;
import com.marry.persistence.mapper.SysUserMapper;
import com.marry.persistence.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock SysUserMapper userMapper;
    @Mock SysUserRoleMapper userRoleMapper;
    @Mock SysDeptMapper deptMapper;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserServiceImpl service;

    private UserDTO dto;

    @BeforeEach
    void setup() {
        dto = new UserDTO();
        dto.setUsername("alice");
        dto.setPassword("P@ssw0rd");
        dto.setNickName("Alice");
        dto.setStatus(1);
    }

    @Test
    void create_rejectsDuplicateUsername() {
        SysUser existing = new SysUser();
        existing.setUsername("alice");
        when(userMapper.selectByUsername("alice")).thenReturn(existing);

        BizException ex = assertThrows(BizException.class, () -> service.create(dto));
        assertEquals(BizCode.USERNAME_EXISTS.getCode(), ex.getCode());
        verify(userMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void create_persistsEncodedPassword() {
        when(userMapper.selectByUsername("alice")).thenReturn(null);
        when(passwordEncoder.encode("P@ssw0rd")).thenReturn("encoded");
        doAnswer(invocation -> {
            SysUser arg = invocation.getArgument(0);
            arg.setId(99L);
            return 1;
        }).when(userMapper).insert(any(SysUser.class));

        service.create(dto);

        verify(userMapper).insert(argThat((SysUser u) ->
                "encoded".equals(u.getPassword()) && u.getStatus() == 1));
    }

    @Test
    void getByUsername_returnsNullOnBlank() {
        assertNull(service.getByUsername(null));
        assertNull(service.getByUsername(""));
        verify(userMapper, never()).selectByUsername(anyString());
    }
}