package com.geelink.connector.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrawlerRequest {
    private String collection;
    private String processor;
    private String pipelineAlias;
    private List<String> urls;
    private String pattern;
    private int depth;
    private int numberOfCrawler = 2;
    private String chromeVersion;
    private String platform; // windows, linux or mac
    private List<ExtractMetaData> extraMetaDataList;
    private String extraContentXPath; // plain,smart,express
    private List<ExtractField> extractFieldList;
}
