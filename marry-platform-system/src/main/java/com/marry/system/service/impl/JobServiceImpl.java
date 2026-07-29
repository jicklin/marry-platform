package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.common.base.PageQuery;
import com.marry.domain.entity.SysJob;
import com.marry.domain.entity.SysJobLog;
import com.marry.persistence.mapper.SysJobLogMapper;
import com.marry.persistence.mapper.SysJobMapper;
import com.marry.system.job.AbstractJobBean;
import com.marry.system.service.IJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements IJobService {

    private final SysJobLogMapper jobLogMapper;
    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ApplicationContext ctx;

    @Override
    public IPage<SysJob> page(PageQuery q, String name, Integer status) {
        LambdaQueryWrapper<SysJob> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) w.like(SysJob::getName, name);
        if (status != null) w.eq(SysJob::getStatus, status);
        w.orderByAsc(SysJob::getId);
        return baseMapper.selectPage(q.toPage(), w);
    }

    @Override
    public IPage<SysJobLog> logPage(PageQuery q, Long jobId, Integer status) {
        LambdaQueryWrapper<SysJobLog> w = new LambdaQueryWrapper<>();
        if (jobId != null) w.eq(SysJobLog::getJobId, jobId);
        if (status != null) w.eq(SysJobLog::getStatus, status);
        w.orderByDesc(SysJobLog::getCreateTime);
        return jobLogMapper.selectPage(q.toPage(), w);
    }

    @Override
    @Transactional
    public void create(SysJob job) {
        if (job.getStatus() == null) job.setStatus(1);
        baseMapper.insert(job);
        scheduleOrCancel(job);
    }

    @Override
    @Transactional
    public void update(SysJob job) {
        if (job.getId() == null) return;
        baseMapper.updateById(job);
        scheduleOrCancel(job);
    }

    @Override
    @Transactional
    public void remove(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) {
            baseMapper.deleteById(id);
            try { cancelJob(id); } catch (Exception ignored) {}
        }
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysJob job = baseMapper.selectById(id);
        if (job == null) return;
        job.setStatus(status);
        baseMapper.updateById(job);
        scheduleOrCancel(job);
    }

    @Override
    public void runOnce(Long id) {
        SysJob job = baseMapper.selectById(id);
        if (job == null) return;
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            // build a one-shot trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("runOnce-" + id + "-" + System.currentTimeMillis())
                    .forJob(jobKeyOf(id))
                    .startNow()
                    .build();
            if (scheduler.checkExists(jobKeyOf(id))) {
                scheduler.triggerJob(jobKeyOf(id), trigger.getJobDataMap());
            }
        } catch (SchedulerException e) {
            log.warn("runOnce failed", e);
        }
    }

    private void scheduleOrCancel(SysJob job) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobKey key = jobKeyOf(job.getId());
            boolean exists = scheduler.checkExists(key);
            if (job.getStatus() != null && job.getStatus() == 1) {
                TriggerKey tk = new TriggerKey("trigger-" + job.getId());
                JobDetail jd = scheduler.getJobDetail(key);
                if (jd == null) {
                    Class<? extends Job> jobClass = resolveJobClass(job.getBeanName());
                    if (jobClass == null) {
                        log.warn("Job bean '{}' not found", job.getBeanName());
                        return;
                    }
                    JobDataMap data = new JobDataMap();
                    data.put("sysJob", job);
                    JobBuilder builder = JobBuilder.newJob(jobClass)
                            .withIdentity(key)
                            .usingJobData(data);
                    jd = builder.storeDurably().build();
                    scheduler.addJob(jd, true);
                }
                Trigger tr = TriggerBuilder.newTrigger()
                        .withIdentity(tk)
                        .withSchedule(CronScheduleBuilder.cronSchedule(job.getCron()).withMisfireHandlingInstructionDoNothing())
                        .forJob(key)
                        .build();
                if (exists) scheduler.rescheduleJob(tk, tr);
                else scheduler.scheduleJob(jd, tr);
            } else if (exists) {
                scheduler.deleteJob(key);
            }
        } catch (SchedulerException e) {
            log.warn("schedule job failed", e);
        }
    }

    private void cancelJob(Long id) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.deleteJob(jobKeyOf(id));
        } catch (SchedulerException ignored) {}
    }

    private JobKey jobKeyOf(Long id) {
        return new JobKey("job-" + id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class<? extends Job> resolveJobClass(String beanName) {
        try {
            Object bean = ctx.getBean(beanName);
            if (bean instanceof AbstractJobBean) return (Class) bean.getClass();
        } catch (Exception ignored) {}
        return null;
    }
}