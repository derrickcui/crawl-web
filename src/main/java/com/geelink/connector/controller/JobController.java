package com.geelink.connector.controller;

import com.geelink.connector.service.QuartzManager;
import com.geelink.connector.job.model.CrawlJobDetail;
import com.geelink.connector.job.model.CrawlJob;
import com.geelink.connector.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {
    private final QuartzManager quartzManager;
    private final MonitorService monitorService;

    /**
     * {
     *   "jobName": "wenxue",
     *   "groupName": "geelink",
     *   "jobClass": "",
     *   "cron": "* 0/10 * ? * * *",
     *   "description": "文学城新闻爬取",
     *   "crawlerRequest": {
     *     "collection": "test",
     *     "processor": "",
     *     "pipelineAlias": "",
     *     "urls": [
     *       "https://www.wenxuecity.com/"
     *     ],
     *     "pattern": "html$",
     *     "depth": 0,
     *     "numberOfCrawler": 1,
     *     "chromeVersion": "",
     *     "platform": "windows",
     *     "extraMetaDataList": null
     *   }
     * }
     * @param crawlJob
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity addJob(@RequestBody CrawlJob crawlJob) {
        quartzManager.addJob(crawlJob);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity listJobs() {
        List<String> jobList = quartzManager.listJobs();

        return ResponseEntity.ok(jobList);
    }

    @GetMapping("/find")
    public ResponseEntity findJobs(String groupName) {
        List<String> jobList = quartzManager.findJobs(groupName);

        return ResponseEntity.ok(jobList);
    }

    @GetMapping("/detail")
    public ResponseEntity<CrawlJobDetail> getJobDetail(String jobName, String groupName) {
        CrawlJobDetail jobDetail = quartzManager.findJobDetail(jobName, groupName);

        return ResponseEntity.ok(jobDetail);
    }

    @GetMapping("/pause")
    public ResponseEntity pauseJob(String jobName, String groupName) {
        quartzManager.pauseJob(jobName, groupName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delete")
    public ResponseEntity deleteJob(String jobName, String groupName) {
        quartzManager.deleteJob(jobName, groupName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/update")
    public ResponseEntity updateJob(String jobName, String groupName, String cron) {
        quartzManager.updateJob(jobName, groupName, cron);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resume")
    public ResponseEntity resumeJob(String jobName, String groupName) {
        quartzManager.resumeJob(jobName, groupName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/terminate")
    public ResponseEntity terminateJob(String jobName, String groupName) {
        quartzManager.terminateJob(jobName, groupName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/monitor")
    public ResponseEntity monitorJob() {
        monitorService.runTaskList();
        return ResponseEntity.ok().build();
    }


}
