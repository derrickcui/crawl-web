package com.geelink.connector.service;

import com.geelink.connector.exception.RequestException;
import com.geelink.connector.exception.ResultEnum;
import com.geelink.connector.exception.UmeException;
import com.geelink.connector.job.model.CrawlJobDetail;
import com.geelink.connector.job.model.CrawlJob;
import com.geelink.connector.util.JobUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.geelink.connector.exception.ResultEnum.*;

@Slf4j
@Component
@Scope("singleton")
public class QuartzManager {

    private static final String JOB_DEFAULT_GROUP_NAME = "JOB_DEFAULT_GROUP_NAME";

    private static final String TRIGGER_DEFAULT_GROUP_NAME = "TRIGGER_DEFAULT_GROUP_NAME";

    private final Scheduler scheduler;


    @Autowired
    public QuartzManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public List<String> listJobs() {
        List<String> jobList = new ArrayList<>();
        try {
            for (String groupName : scheduler.getJobGroupNames()) {

                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();

                    //get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Date nextFireTime = triggers.get(0).getNextFireTime();

                    log.info("[jobName] : " + jobName + " [groupName] : "
                            + jobGroup + " - " + nextFireTime);
                    jobList.add("[jobName] : " + jobName + " [groupName] : "
                            + jobGroup + " - " + nextFireTime);

                }
            }
        } catch (SchedulerException e) {
            log.error("Failed to list all jobs and groups", e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }

        return jobList;
    }

    public List<String> findJobs(String groupName) {
        if (StringUtils.isBlank(groupName)) {
            log.error("groupName is missing");
            throw new RequestException(ResultEnum.REQUEST_MISSING_PARAMETER, "groupName");
        }

        List<String> jobList = new ArrayList<>();
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String name = jobKey.getName();
                String group = jobKey.getGroup();

                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Date nextFireTime = triggers.get(0).getNextFireTime();

                log.info("[jobName] : " + name + " [groupName] : "
                        + group + " - " + nextFireTime);
                jobList.add("[jobName] : " + name + " [groupName] : "
                        + group + " - " + nextFireTime);
            }
        } catch (SchedulerException e) {
            log.error("Failed to find job from group:{}", groupName, e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }

        return jobList;
    }

    public CrawlJobDetail findJobDetail(String jobName, String groupName) {

        try {
            JobDetail jobDetail = scheduler.getJobDetail(getJobKey(jobName, groupName));
            if (jobDetail == null) {
                log.error("job:{} is not existing in group:{}", jobName, groupName);
                throw new UmeException(RESPONSE_NOT_FOUND);
            }


            CrawlJobDetail result = new CrawlJobDetail();
            result.setJobName(jobName);
            result.setGroupName(groupName);
            result.setDescription(jobDetail.getDescription());
            if (jobDetail.getJobDataMap() != null) {
                result.setCrawlerRequest(JobUtil.convertJobDataMapToCrawlRequest(jobDetail.getJobDataMap()));
            }

            List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(getJobKey(jobName, groupName));
            result.setJobClass(jobDetail.getJobClass().getName());
            result.setCron(((CronTriggerImpl) triggers.get(0)).getCronExpression());
            result.setStartTime(triggers.get(0).getStartTime());
            result.setEndTime(triggers.get(0).getEndTime());
            result.setNextFireTime(triggers.get(0).getNextFireTime());
            result.setPreviousFireTime(triggers.get(0).getPreviousFireTime());
            return result;
        } catch (SchedulerException e) {
            log.error("Failed to get job detail from scheduler", e);
            throw new UmeException(QUARTZ_EXCEPTION);
        }
    }

    public void addJob(CrawlJob crawlJob) {
        if (StringUtils.isBlank(crawlJob.getJobClass())) crawlJob.setJobClass("com.geelink.connector.job.CrawlWebJob");

        // convert CrawlRequest to JobDataMap
        JobDataMap jobDataMap = JobUtil.convertCrawlTaskToJobDataMap(crawlJob);

        addJob(crawlJob, jobDataMap);
    }

    private void addJob(CrawlJob crawlJob, JobDataMap jobDataMap) {
        if (!CronExpression.isValidExpression(crawlJob.getCron())) {
            log.error("Illegal cron expression format({})", crawlJob.getCron());
            throw new RequestException(ResultEnum.REQUEST_INVALID_PARAMETER, "Illegal cron expression format");
        }

        JobKey jobKey = getJobKey(crawlJob.getJobName(), crawlJob.getGroupName());
        try {
            JobDetail jobDetail = JobBuilder.newJob().withIdentity(jobKey)
                    .ofType((Class<Job>) Class.forName(crawlJob.getJobClass()))
                    .withDescription(crawlJob.getDescription())
                    .setJobData(jobDataMap)
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(crawlJob.getCron()))
                    .withIdentity(new TriggerKey(crawlJob.getJobName(), StringUtils.isNotBlank(crawlJob.getGroupName())? crawlJob.getGroupName(): TRIGGER_DEFAULT_GROUP_NAME))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (Exception e) {
            log.error("QuartzManager add job failed", e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }
    }

    public void updateJob(String jobName, String groupName, String cronExp) {
        if (!CronExpression.isValidExpression(cronExp)) {
            log.error("Illegal cron expression format({})", cronExp);
            throw new RequestException(ResultEnum.REQUEST_INVALID_PARAMETER, "Illegal cron expression format");
        }

        JobKey jobKey = getJobKey(jobName, groupName);
        TriggerKey triggerKey = new TriggerKey(jobName, StringUtils.isNotBlank(groupName)? groupName: TRIGGER_DEFAULT_GROUP_NAME);
        try {
            if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                Trigger newTrigger = TriggerBuilder.newTrigger()
                        .forJob(jobDetail)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExp))
                        .withIdentity(new TriggerKey(jobName, TRIGGER_DEFAULT_GROUP_NAME))
                        .build();
                scheduler.rescheduleJob(triggerKey, newTrigger);
            } else {
                log.error("update job name:{},group name:{} or trigger name:{},group name:{} not exists..",
                        jobKey.getName(), jobKey.getGroup(), triggerKey.getName(), triggerKey.getGroup());

                throw new UmeException(QUARTZ_NOT_FOUND, jobKey.getGroup() + "/" + jobKey.getName());
            }
        } catch (SchedulerException e) {
            log.error("update job name:{},group name:{} failed!", jobKey.getName(), jobKey.getGroup(), e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }
    }

    public void deleteJob(String jobName, String groupName) {
        JobKey jobKey = getJobKey(jobName, groupName);
        try {
            if (scheduler.checkExists(jobKey)) {
                if(scheduler.deleteJob(jobKey)) {
                    log.info("Delete job:{} from group:{} successfully", jobName, groupName);
                } else {
                    log.error("Failed to delete job:{} from group:{}", jobName, groupName);
                    throw new UmeException(QUARTZ_EXCEPTION, "return false");
                }
            } else {
                log.error("delete job name:{},group name:{} not exists.", jobKey.getName(), jobKey.getGroup());
                throw new UmeException(ResultEnum.RESPONSE_NOT_FOUND, "Job is not existing");
            }
        } catch (SchedulerException e) {
            log.error("delete job name:{},group name:{} failed!", jobKey.getName(), jobKey.getGroup(), e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }
    }

    public void pauseJob(String jobName, String groupName) {
        JobKey jobKey = getJobKey(jobName, groupName);
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                log.warn("Job:{} is not found in group:{}", jobName, groupName);
                throw new UmeException(ResultEnum.RESPONSE_NOT_FOUND, "Job is not existing");
            }

            scheduler.pauseJob(jobKey);
            log.info("pause job:{} group:{} successfully", jobName, groupName);
        } catch (SchedulerException e) {
            log.error("pause job name:{},group name:{} failed!", jobKey.getName(), jobKey.getGroup(), e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }
    }

    public void resumeJob(String jobName, String groupName) {
        JobKey jobKey = getJobKey(jobName, groupName);
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                log.warn("Job:{} is not found in group:{}", jobName, groupName);
                throw new UmeException(ResultEnum.RESPONSE_NOT_FOUND, "Job is not existing");
            }

            scheduler.resumeJob(jobKey);
            log.info("resume job:{} group:{} successfully", jobName, groupName);
        } catch (SchedulerException e) {
            log.error("resume job name:{},group name:{} failed!", jobKey.getName(), jobKey.getGroup(), e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }
    }

    public void terminateJob(String jobName, String groupName) {
        JobKey jobKey = getJobKey(jobName, groupName);
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                log.warn("Job:{} is not found in group:{}", jobName, groupName);
                throw new UmeException(ResultEnum.RESPONSE_NOT_FOUND, "Job is not existing");
            }

            if (scheduler.interrupt(jobKey)) {
                log.info("Terminate job:{} group:{} successfully", jobName, groupName);
            } else {
                log.error("Terminate job:{} group:{} failed", jobName, groupName);
            }
        } catch (SchedulerException e) {
            log.error("terminate job name:{},group name:{} failed!", jobKey.getName(), jobKey.getGroup(), e);
            throw new UmeException(QUARTZ_EXCEPTION, e.getMessage());
        }
    }

    private JobKey getJobKey(String jobName, String groupName) {
        if(StringUtils.isBlank(jobName)) {
            log.error("JobName is missing");
            throw new RequestException(ResultEnum.REQUEST_MISSING_PARAMETER, "JobName");
        }

        return new JobKey(jobName, StringUtils.isNotBlank(groupName)? groupName: JOB_DEFAULT_GROUP_NAME);
    }
}
