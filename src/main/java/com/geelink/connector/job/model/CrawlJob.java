package com.geelink.connector.job.model;

import com.geelink.connector.model.CrawlerRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlJob {
    private String jobName;
    private String groupName;
    private String jobClass;
    private String cron;
    private String description;

    private CrawlerRequest crawlerRequest;
}
