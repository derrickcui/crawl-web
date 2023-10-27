package com.geelink.connector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geelink.connector.exception.UmeException;
import com.geelink.connector.job.model.CrawlJob;
import com.geelink.connector.model.CrawlerRequest;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;

import static com.geelink.connector.exception.ResultEnum.JSON_CONVERT_ERROR;

@Slf4j
public final class JobUtil {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    public static JobDataMap convertCrawlTaskToJobDataMap(CrawlJob task) {
        if (task == null) return null;

        JobDataMap jobDataMap = new JobDataMap();
        try {
            jobDataMap.put("crawlRequest", objectMapper.writeValueAsString(task.getCrawlerRequest()));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse crawl request", e);
            throw new UmeException(JSON_CONVERT_ERROR, "convert crawl request to string");
        }

        return jobDataMap;
    }

    public static CrawlerRequest convertJobDataMapToCrawlRequest(JobDataMap jobDataMap) {
        if (jobDataMap == null || !jobDataMap.containsKey("crawlRequest")) return null;

        try {
            return objectMapper.readValue(jobDataMap.getString("crawlRequest"), CrawlerRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse crawl request", e);
            throw new UmeException(JSON_CONVERT_ERROR, "convert crawl request to string");
        }
    }
}
