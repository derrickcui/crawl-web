package com.geelink.connector.job.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CrawlJobDetail extends CrawlJob {
    private Date endTime;
    private Date nextFireTime;
    private Date startTime;
    private Date previousFireTime;
}
