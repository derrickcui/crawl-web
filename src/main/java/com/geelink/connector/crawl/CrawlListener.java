package com.geelink.connector.crawl;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.SpiderListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CrawlListener implements SpiderListener {
    private final AtomicInteger successCount = new AtomicInteger(0);

    private final AtomicInteger errorCount = new AtomicInteger(0);

    private final List<String> errorUrls = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onSuccess(Request request) {
        successCount.incrementAndGet();
    }

    @Override
    public void onError(Request request) {
        errorUrls.add(request.getUrl());
        errorCount.incrementAndGet();
    }

    public AtomicInteger getSuccessCount() {
        return successCount;
    }

    public AtomicInteger getErrorCount() {
        return errorCount;
    }

    public List<String> getErrorUrls() {
        return errorUrls;
    }

    @Override
    public void onError(Request request, Exception e) {
        log.error("crawl failed: {}", request.getUrl());
        SpiderListener.super.onError(request, e);
    }
}
