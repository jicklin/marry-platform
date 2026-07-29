package com.marry.log.service;

import com.marry.domain.entity.SysOperLog;
import com.marry.persistence.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Thin facade for the operation log mapper, living in the log module so the aspect
 * doesn't need to depend on the system module.
 */
@Service
@RequiredArgsConstructor
public class OperLogService {

    private final SysOperLogMapper operLogMapper;

    public void save(SysOperLog row) {
        operLogMapper.insert(row);
    }
}