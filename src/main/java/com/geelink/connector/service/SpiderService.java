package com.geelink.connector.service;


import com.geelink.connector.config.AppConfig;
import com.geelink.connector.crawl.CrawlListener;
import com.geelink.connector.crawl.IndexPipeline;
import com.geelink.connector.crawl.UmeFileScheduler;
import com.geelink.connector.exception.RequestException;
import com.geelink.connector.exception.ResultEnum;
import com.geelink.connector.model.CrawlerRequest;
import com.geelink.connector.crawl.processor.GenericProcessor;
import com.geelink.connector.crawl.processor.WenshuCourtProcessor;
import com.geelink.connector.util.JsoupUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SpiderService {
    private final ApplicationContext context;
    private final AppConfig appConfig;
    private final JsoupUtil jsoupUtil;

    private final MonitorService monitorService;

    @Autowired
    public SpiderService(ApplicationContext context, AppConfig appConfig, JsoupUtil jsoupUtil, MonitorService monitorService) {
        this.context = context;
        this.appConfig = appConfig;
        this.jsoupUtil = jsoupUtil;
        this.monitorService = monitorService;
    }

    /**
     * Crawling web
     *
     * @param request crawl web request
     */
    public void fetchWeb(CrawlerRequest request) {
        if ( CollectionUtils.isEmpty(request.getUrls()) ) {
            log.error("website url list is empty, do nothing");
            throw new RequestException(ResultEnum.REQUEST_MISSING_PARAMETER, "urls");
        }

        if ( StringUtils.isBlank(request.getCollection()) ) {
            log.error("collection is empty, do nothing");
            throw new RequestException(ResultEnum.REQUEST_MISSING_PARAMETER, "collection");
        }

        PageProcessor pageProcessor;
        if ( request.getProcessor() != null && request.getProcessor().equals("gaojian") ) {
            pageProcessor = context.getBean(WenshuCourtProcessor.class, request, appConfig.getChromeDriverFolder());
            ((WenshuCourtProcessor)pageProcessor).login();
        } else {
            pageProcessor = context.getBean(GenericProcessor.class, request, jsoupUtil);
        }

        IndexPipeline indexPipeline = context.getBean(IndexPipeline.class, request.getCollection(), request.getPipelineAlias(), appConfig.getOutputFolder());

        Spider spider = Spider.create(pageProcessor);
        spider.addUrl(request.getUrls().toArray(new String[0]));
        spider.addPipeline(indexPipeline);
        spider.setScheduler(new UmeFileScheduler(appConfig.getCacheFolder(), request.getDepth() < 0? appConfig.getDefaultDepth(): request.getDepth()));
        spider.thread(request.getNumberOfCrawler());
        List< SpiderListener> listenerList = new ArrayList<>();
        listenerList.add(new CrawlListener());
        spider.setSpiderListeners(listenerList);
        //spider.setDownloader(new SeleniumDownloader());
        spider.setUUID(UUID.randomUUID().toString());
        spider.setExitWhenComplete(true);
        monitorService.register(spider);
        spider.run();
    }
}
