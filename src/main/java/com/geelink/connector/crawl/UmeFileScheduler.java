package com.geelink.connector.crawl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

@Slf4j
public class UmeFileScheduler extends FileCacheQueueScheduler {
    private int levelLimit = -1;
    private String LEVEL_KEY = "_level";

    public UmeFileScheduler(String filePath, int levelLimit) {
        super(filePath);
        this.levelLimit = levelLimit;
    }

    @Override
    protected void pushWhenNoDuplicate(Request request, Task task) {
        if ( levelLimit <= 0 || MapUtils.isEmpty(request.getExtras()) ) {
            super.pushWhenNoDuplicate(request, task);
            return;
        }

        if (request.getExtras().containsKey(LEVEL_KEY)
                && (Integer)request.getExtra(LEVEL_KEY) <= levelLimit) {

            super.pushWhenNoDuplicate(request, task);
        } else {
            log.warn("level：{} is not expected, skip it:{}", request.getExtra(LEVEL_KEY), request.getUrl());
        }
    }
}
