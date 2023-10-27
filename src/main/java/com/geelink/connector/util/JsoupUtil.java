package com.geelink.connector.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JsoupUtil {
    public Map<String, String> extractMetaData(Elements elements) {
        Map<String, String> result = new HashMap<>();
        for(Element element:elements) {
            String name = element.attr("name");
            if (StringUtils.isNotBlank(name)) {
                result.put(element.attr("name"), element.attr("content"));
                continue;
            }

            name = element.attr("property");
            if (StringUtils.isNotBlank(name)) {
                result.put(element.attr("property"), element.attr("content"));
            }
        }
        return result;
    }

    public Elements extraMetaElements(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.select("head").select("meta");
        } catch (IOException e) {
            log.error("failed to extract meta for url:{}", url, e);
        }

        return null;
    }

    public Elements extractMetaElements(Page page) {
        return Jsoup.parse(page.getHtml().get()).select("head").select("meta");
    }
}
