package com.geelink.connector.util;

import com.geelink.connector.model.CrawlerRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import us.codecraft.webmagic.Page;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import static com.geelink.connector.crawl.LevelLimitScheduler.LEVEL_KEY;

@Slf4j
public final class PageUtil {
    private final static int defaultDepth = 0;
    public static final String DEFAULT_PATTERN = "html$";
    public static final String FIELD_GL_SOURCE = "_gl_source";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_ID = "id";
    public static final String FIELD_GL_COLLECTION = "_gl_collection";

    public static String getDomainName(String url) {
        if(!url.startsWith("http") && !url.startsWith("https")){
            url ="http://" + url;
        }

        try {
            URL netUrl = new URL(url);
            String host = netUrl.getHost();
            if(host.startsWith("www")){
                host = host.substring("www".length()+1);
            }

            return host;
        } catch (MalformedURLException e) {
            log.error("Failed to extract domain name from url:{}", url);
        }

        return null;
    }

    public static boolean isMatch(CrawlerRequest request, String url, Pattern urlPattern) {
        return request.getUrls().stream().anyMatch(t -> urlPattern.matcher(url).find());
    }

    public static int getLevel(Page page) {
        if ( MapUtils.isEmpty(page.getRequest().getExtras()) ) {
            return defaultDepth;
        }

        if ( !page.getRequest().getExtras().containsKey(LEVEL_KEY) ) {
            return defaultDepth;
        }

        return page.getRequest().getExtra(LEVEL_KEY);
    }


}
