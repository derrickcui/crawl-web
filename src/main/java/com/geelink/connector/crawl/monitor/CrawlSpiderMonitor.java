package com.geelink.connector.crawl.monitor;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.monitor.SpiderStatusMXBean;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlSpiderMonitor {
        private final static CrawlSpiderMonitor INSTANCE = new CrawlSpiderMonitor();

        private final AtomicBoolean started = new AtomicBoolean(false);

        private final MBeanServer mbeanServer;

        private final String jmxServerName;

        private final Map<String, CrawlSpiderStatus> spiderStatuses = new HashMap<>();

        protected CrawlSpiderMonitor() {
            jmxServerName = "WebMagic";
            mbeanServer = ManagementFactory.getPlatformMBeanServer();
        }
        public Map<String, CrawlSpiderStatus> getSpiderStatuses()
        {
            return spiderStatuses;
        }

        /**
         * Register spider for monitor.
         *
         * @param spiders spiders
         * @return this
         */
        public synchronized CrawlSpiderMonitor register(Spider... spiders) throws JMException {
            for (Spider spider : spiders) {
                MyMonitorSpiderListener monitorSpiderListener = new MyMonitorSpiderListener();
                if (spider.getSpiderListeners() == null) {
                    List<SpiderListener> spiderListeners = new ArrayList<>();
                    spiderListeners.add(monitorSpiderListener);
                    spider.setSpiderListeners(spiderListeners);
                } else {
                    spider.getSpiderListeners().add(monitorSpiderListener);
                }
                CrawlSpiderStatus spiderStatusMBean = getSpiderStatusMBean(spider, monitorSpiderListener);
                registerMBean(spiderStatusMBean);
                spiderStatuses.put(spider.getUUID(),spiderStatusMBean);
            }
            return this;
        }

        protected CrawlSpiderStatus getSpiderStatusMBean(Spider spider, MyMonitorSpiderListener monitorSpiderListener) {
            return new CrawlSpiderStatus(spider, monitorSpiderListener);
        }

        public static CrawlSpiderMonitor instance() {
            return INSTANCE;
        }

        public static class MyMonitorSpiderListener implements SpiderListener {

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
        }

        protected void registerMBean(SpiderStatusMXBean spiderStatus) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
            ObjectName objName = new ObjectName(jmxServerName + ":name=" + spiderStatus.getName());
            if(mbeanServer.isRegistered(objName)==false)
            {
                mbeanServer.registerMBean(spiderStatus, objName);
            }
        }

}
