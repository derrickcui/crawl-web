package com.geelink.connector.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaRequest {
    private String url;
    private int depth;
    private String processor;
    private String platform;
    private String chromeVersion;
    private int numOfPage;
}
