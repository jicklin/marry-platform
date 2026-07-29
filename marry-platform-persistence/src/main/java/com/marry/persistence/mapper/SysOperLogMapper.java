package com.marry.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marry.domain.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    Long countToday();

    Long countBetween(@org.apache.ibatis.annotations.Param("begin") LocalDateTime begin,
                      @org.apache.ibatis.annotations.Param("end") LocalDateTime end);

    List<Map<String, Object>> countByBusinessType(@org.apache.ibatis.annotations.Param("days") Integer days);

    int cleanBefore(@org.apache.ibatis.annotations.Param("cutoff") LocalDateTime cutoff);
}