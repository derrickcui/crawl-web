package com.geelink.connector.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public abstract class BaseJob implements Job {
    protected abstract void executeInternal(JobExecutionContext context);

    protected String cronExpression;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            executeInternal(context);
        } catch (Exception e) {
            log.error("job 执行失败！", e);
        }
    }

    public String getCronExpression() {
        return cronExpression;
    }
}
