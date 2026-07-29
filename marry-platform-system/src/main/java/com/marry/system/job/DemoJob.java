package com.marry.system.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simple demo job used by the scheduler test. Logs each invocation.
 */
@Slf4j
@Component("demoJob")
public class DemoJob extends AbstractJobBean {

    @Override
    protected void doExecute() {
        log.info("[demoJob] tick at {}", java.time.LocalDateTime.now());
    }
}