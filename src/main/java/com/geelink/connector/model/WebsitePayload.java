package com.geelink.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebsitePayload extends Payload {
    private String url;
    private String pattern = "html$";
    private int depth = -1;
    private int maxPage = 100;

    @Override
    public boolean validate() {
        return StringUtils.isNoneBlank(url);
    }
}
