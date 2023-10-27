package com.geelink.connector.crawl.processor;

import com.geelink.connector.model.CrawlerRequest;
import com.geelink.connector.model.ExtractMetaData;
import com.geelink.connector.util.JsoupUtil;
import com.geelink.connector.service.MetaDataService;
import com.geelink.connector.util.PageUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.geelink.connector.crawl.LevelLimitScheduler.LEVEL_KEY;
import static com.geelink.connector.util.PageUtil.*;

@Getter
@Setter
@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GenericProcessor implements PageProcessor {
    private CrawlerRequest request;
    private Pattern urlPattern;
    private List<String> domainList;
    private MetaDataService metaDataService;
    private JsoupUtil jsoupUtil;

    public GenericProcessor(CrawlerRequest request,JsoupUtil jsoupUtil) {
        this.request = request;
        String pattern = StringUtils.isBlank(request.getPattern())? PageUtil.DEFAULT_PATTERN: request.getPattern();
        this.urlPattern = Pattern.compile(pattern);
        this.domainList = request.getUrls().stream().map(PageUtil::getDomainName).filter(Objects::nonNull).collect(Collectors.toList());
        this.jsoupUtil = jsoupUtil;
    }

    private Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setTimeOut(10000)
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");

    @Override
    public void process(Page page) {
        int currentLevel = PageUtil.getLevel(page);

        List<String> subLinks = page.getHtml().links().all();

        if (CollectionUtils.isNotEmpty(subLinks) ) {
            List<Request> targets = subLinks.stream()
                    .filter(t -> domainList.stream().anyMatch(t::contains))
                        .map(Request::new)
                        .map(r -> r.putExtra(LEVEL_KEY, currentLevel + 1))
                        .toList();
            targets.forEach(page::addTargetRequest);
        }

        if (PageUtil.isMatch(request, page.getUrl().get(), urlPattern)) {
            Map<String, Object> metadata = extractHeader(page);
            metadata.forEach(page::putField);

            try {
                page.putField(FIELD_GL_SOURCE, page.getUrl().toString());
                page.putField(FIELD_TITLE, page.getHtml().xpath("//title")
                        .get().replace("<title>", "")
                        .replace("</title>", ""));
                if (StringUtils.isBlank(request.getExtraContentXPath()) || "smart".equalsIgnoreCase(request.getExtraContentXPath())) {
                    page.putField(FIELD_CONTENT, page.getHtml().smartContent().get());
                } else if ("plain".equalsIgnoreCase(request.getExtraContentXPath())) {
                    page.putField(FIELD_CONTENT, page.getHtml().get());
                } else if ("xpath".equalsIgnoreCase(request.getExtraContentXPath())) {
                    page.putField("content", page.getHtml().xpath(request.getExtraContentXPath()));
                } else {
                    page.putField(FIELD_CONTENT, page.getHtml().smartContent().get());
                }

                if (CollectionUtils.isNotEmpty(request.getExtractFieldList())) {
                    request.getExtractFieldList().stream().filter(m -> StringUtils.isNotBlank(m.getFieldName()) && StringUtils.isNotBlank(m.getXpath())).forEach(field ->
                        page.putField(field.getFieldName(), page.getHtml().xpath(field.getXpath()))
                    );
                }
            } catch (Exception e) {
                log.error("Failed to read page:{}, ignore it", page.getUrl().get(), e);
            }
        } else {
            page.setSkip(true);
        }
    }

    private Map<String, Object> extractHeader(Page page) {
        if (CollectionUtils.isEmpty(request.getExtraMetaDataList())) return new HashMap<>();

        Elements elements = jsoupUtil.extractMetaElements(page);
        return extractMeta(elements, request.getExtraMetaDataList());
    }

    private Map<String, Object> extractMeta(Elements elements, List<ExtractMetaData> extractMetaDataList) {
        Map<String, String> additionalField = extractMetaDataList.stream().collect(Collectors.toMap(ExtractMetaData::getSource, ExtractMetaData::getDestination));
        Map<String, Object> result = new HashMap<>();
        if (MapUtils.isEmpty(additionalField)) return result;

        for(Element element:elements) {
            String name = element.attr("name");
            if (StringUtils.isNotBlank(name) && additionalField.containsKey(name)) {
                result.put(additionalField.get(element.attr("name")), element.attr("content"));
                continue;
            }

            name = element.attr("property");
            if (StringUtils.isNotBlank(name) && additionalField.containsKey(name)) {
                result.put(additionalField.get(element.attr("property")), element.attr("content"));
            }
        }

        return result;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
