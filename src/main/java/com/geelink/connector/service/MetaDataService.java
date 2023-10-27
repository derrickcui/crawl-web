package com.geelink.connector.service;

import com.geelink.connector.exception.RequestException;
import com.geelink.connector.exception.UmeException;
import com.geelink.connector.model.MetaRequest;
import com.geelink.connector.model.MetaResponse;
import com.geelink.connector.util.JsoupUtil;
import com.geelink.connector.util.LinkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.geelink.connector.exception.ResultEnum.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetaDataService {
    private final JsoupUtil jsoupUtil;

    public MetaResponse fetchMetadata(MetaRequest request) {
        if (StringUtils.isBlank(request.getUrl())) {
            log.error("website url is empty, do nothing");
            throw new RequestException(REQUEST_MISSING_PARAMETER, "Missing parameter: url");
        }

        String rootUrl = StringUtils.trim(request.getUrl());
        if (!rootUrl.startsWith("http://") && !rootUrl.startsWith("https://")) {
            log.error("website url is invalid, must be start with http:// or https://, do nothing");
            throw new RequestException(REQUEST_INVALID_PARAMETER, "url is not valid, please use http:// or https://");
        }

        // if the depth is not provided(default 0), default to 1
        if (request.getDepth() == 0) request.setDepth(1);
        // if the number of page is not provided(default 0), default to 10
        if (request.getNumOfPage() == 0) request.setNumOfPage(10);

        log.info("Start to get mete data from url:{}, depth:{}, number of page:{}", rootUrl, request.getDepth(), request.getNumOfPage());
        LinkUtil linkUtil = new LinkUtil(request.getDepth());
        linkUtil.getPageLinks(rootUrl);
        Set<String> links = linkUtil.getLinks();

        List<Elements> elementsList;
        if (CollectionUtils.isEmpty(links)) {
            // get current page's meta
            elementsList = Collections.singletonList(jsoupUtil.extraMetaElements(rootUrl));
        } else {
            // get all pages meta
            elementsList = links.stream().limit(request.getNumOfPage()).map(jsoupUtil::extraMetaElements).toList();
        }

        if (CollectionUtils.isEmpty(elementsList)) {
            log.error("No meta elements is found, return empty");
            throw new UmeException(RESPONSE_NOT_FOUND, "No meta elements is found, return empty");
        }

        List<Map<String, String>> metaMap = elementsList.stream().map(jsoupUtil::extractMetaData).toList();
        return new MetaResponse( elementsList.stream().map(m -> "<header>" + m.toString() + "</header>").toList(), metaMap);
    }


}
