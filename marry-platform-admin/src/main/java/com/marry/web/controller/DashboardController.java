package com.marry.web.controller;

import com.marry.api.vo.dashboard.DashboardStatsVO;
import com.marry.common.core.domain.R;
import com.marry.persistence.mapper.*;
import com.marry.security.properties.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@Tag(name = "数据看板")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserMapper userMapper;
    private final SysOperLogMapper operLogMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    @Operation(summary = "统计数据")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/stats")
    public R<DashboardStatsVO> stats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setTodayOperLog(operLogMapper.countToday());
        vo.setTodayLoginCount(loginLogMapper.countToday());

        Set<String> keys = redisTemplate.keys(jwtProperties.getOnlineKey() + ":*");
        vo.setOnlineCount(keys == null ? 0L : (long) keys.size());

        // 7-day visit trend (login counts by day)
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Long cnt = loginLogMapper.countBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date.toString());
            row.put("count", cnt == null ? 0L : cnt);
            trend.add(row);
        }
        vo.setVisitTrend(trend);

        vo.setOperTypeDist(operLogMapper.countByBusinessType(7));
        vo.setLoginStatusDist(loginLogMapper.countByStatus(7));

        return R.ok(vo);
    }
}