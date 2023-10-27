package com.geelink.connector.crawl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.PriorityScheduler;

@Slf4j
public class LevelLimitScheduler extends PriorityScheduler {
    public static int levelLimit = -1;
    public static final String LEVEL_KEY = "_level";

    public LevelLimitScheduler(int levelLimit) {
        LevelLimitScheduler.levelLimit = levelLimit;
    }


    @Override
    public synchronized void push(Request request, Task task) {
        if ( levelLimit <= 0 || MapUtils.isEmpty(request.getExtras()) ) {
            super.push(request, task);
            return;
        }

        if (request.getExtras().containsKey(LEVEL_KEY)
                && (Integer)request.getExtra(LEVEL_KEY) <= levelLimit) {
            super.push(request, task);
        } else {
            log.warn("level：{} is not expected, skip it:{}", request.getExtra(LEVEL_KEY), request.getUrl());
        }
    }

    @Override
    public void pushWhenNoDuplicate(Request request, Task task) {
        super.pushWhenNoDuplicate(request, task);
    }
}
