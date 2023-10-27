package com.geelink.connector.service;

import com.geelink.connector.crawl.monitor.CrawlSpiderMonitor;
import com.geelink.connector.crawl.monitor.CrawlSpiderStatus;
import com.geelink.connector.crawl.monitor.TaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

import javax.management.JMException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MonitorService {
    private final CrawlSpiderMonitor monitor = CrawlSpiderMonitor.instance();
    public void register(Spider spider) {
        try {
            monitor.register(spider);
        } catch (JMException e) {
            log.error("Failed to register spider", e);
        }
    }

    public void runTaskList() {

        Map<String, CrawlSpiderStatus> spiderStatuses = monitor.getSpiderStatuses();

        log.info("test");
      /*  List<TaskDTO> taskDTOList = monitor.getSpiderStatuses();
        for (TaskDTO taskDTO : taskDTOList) {
            CrawlSpiderStatus spiderStatus = spiderStatuses.get(taskDTO.getSpiderUUID());
            if (spiderStatus == null) {
                taskDTO.setRunState(Spider.Status.Stopped.name());
            } else {
                taskDTO.setRunState(spiderStatus.getStatus());
            }
        }

        return taskDTOList;*/
    }

    public TaskDTO stop(TaskDTO taskDTO) {
        Map<String, CrawlSpiderStatus> spiderStatuses = monitor.getSpiderStatuses();
        CrawlSpiderStatus spiderStatus = spiderStatuses.get(taskDTO.getSpiderUUID());

        if (spiderStatus != null) {
            spiderStatus.stop();
            spiderStatus.getSpider().close();
        }

        return taskDTO;
    }
}
