package com.geelink.connector.job;

import com.geelink.connector.model.CrawlerRequest;
import com.geelink.connector.service.SpiderService;
import com.geelink.connector.util.JobUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CrawlWebJob")
@DisallowConcurrentExecution
public class CrawlWebJob extends BaseJob {
    @Autowired
    private SpiderService spiderService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("{} 开始执行", context.getJobDetail().getKey().getName());
        CrawlerRequest crawlRequest = JobUtil.convertJobDataMapToCrawlRequest(context.getJobDetail().getJobDataMap());
        spiderService.fetchWeb(crawlRequest);
        log.info("details:{}", context.getJobDetail().getJobDataMap());
        log.info("{} 执行结束", context.getJobDetail().getKey().getName());
    }
}
