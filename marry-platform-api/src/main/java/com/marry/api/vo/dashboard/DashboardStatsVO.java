package com.marry.api.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Aggregated dashboard statistics.
 */
@Data
@Schema(description = "Dashboard statistics payload")
public class DashboardStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Total registered users")
    private Long userCount;

    @Schema(description = "Currently online users")
    private Long onlineCount;

    @Schema(description = "Operation logs recorded today")
    private Long todayOperLog;

    @Schema(description = "Login count today")
    private Long todayLoginCount;

    @Schema(description = "Visit trend, last 7 days: date -> count")
    private List<Map<String, Object>> visitTrend;

    @Schema(description = "Operation count by business type")
    private List<Map<String, Object>> operTypeDist;

    @Schema(description = "Login success vs fail distribution")
    private List<Map<String, Object>> loginStatusDist;
}