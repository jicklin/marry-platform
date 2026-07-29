package com.marry.system.job;

import com.marry.domain.entity.SysJob;
import com.marry.domain.entity.SysJobLog;
import com.marry.persistence.mapper.SysJobLogMapper;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;

/**
 * Base class for scheduled jobs. Subclasses implement {@link #doExecute()}.
 * Captures status, duration, error message and persists a {@link SysJobLog} row.
 */
public abstract class AbstractJobBean extends QuartzJobBean {

    @Autowired
    protected SysJobLogMapper jobLogMapper;

    protected abstract void doExecute();

    @Override
    protected void executeInternal(JobExecutionContext context) {
        SysJob job = (SysJob) context.getMergedJobDataMap().get("sysJob");
        long start = System.currentTimeMillis();
        SysJobLog log = new SysJobLog();
        if (job != null) {
            log.setJobId(job.getId());
            log.setJobName(job.getName());
            log.setBeanName(job.getBeanName());
            log.setMethodName(job.getMethodName());
            log.setParams(job.getParams());
        }
        try {
            doExecute();
            log.setStatus(1);
        } catch (Throwable e) {
            log.setStatus(0);
            String msg = e.getMessage();
            log.setErrorMsg(msg == null ? e.getClass().getSimpleName() : msg);
        } finally {
            log.setCostTime(System.currentTimeMillis() - start);
            try {
                jobLogMapper.insert(log);
            } catch (Exception ignored) {}
        }
    }
}