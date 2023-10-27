package com.geelink.connector.crawl.monitor;

import lombok.Getter;

@Getter
public enum StatusEnum implements CodeEnum{

    YES(1, "是"),
    NO(2, "否"),;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;

    private String desc;

}
