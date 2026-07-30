package com.marry.security.filter;

import com.marry.common.security.DataScopeContext;
import com.marry.persistence.mapper.SysRoleDeptMapper;
import com.marry.persistence.mapper.SysRoleMapper;
import com.marry.persistence.mapper.SysUserMapper;
import com.marry.common.security.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Populates the per-request {@link DataScopeContext} after JwtAuthenticationFilter
 * has placed a {@link LoginUser} in the SecurityContext.
 *
 * <p>Cleared in a finally block to prevent leakage to the next request that
 * reuses the servlet container's worker thread.</p>
 */
@Component
@Order(2) // after JwtAuthenticationFilter
@RequiredArgsConstructor
public class DataScopeEnrichFilter extends OncePerRequestFilter {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            LoginUser u = com.marry.security.util.SecurityUtil.currentUser();
            if (u != null && u.getUserId() != null) {
                // data scopes
                List<Long> roleIds = userMapper.selectRoleIdsByUserId(u.getUserId());
                List<Integer> scopes = roleMapper.selectBatchIds(roleIds).stream()
                        .map(r -> r.getDataScope())
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList();
                // custom depts
                Set<Long> customDepts = new HashSet<>();
                for (Long rid : roleIds) {
                    var r = roleMapper.selectById(rid);
                    if (r != null && r.getDataScope() != null && r.getDataScope() == 5) {
                        roleDeptMapper.selectList(
                                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.marry.domain.entity.SysRoleDept>()
                                        .eq("role_id", rid)
                        ).forEach(rd -> customDepts.add(rd.getDeptId()));
                    }
                }
                DataScopeContext.set(scopes, customDepts, u.getUsername(), u.getDeptId());
            }
            chain.doFilter(request, response);
        } finally {
            DataScopeContext.clear();
        }
    }
}