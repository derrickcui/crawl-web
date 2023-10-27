package com.geelink.connector.crawl.monitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TaskDTO {
    private Long id;

    private Long siteId;

    private String taskName;

    private String runState;

    private Integer timerTask;

    private Integer runTask;

    private Date startTime;

    private Date endTime;

    private String taskRuleJson;

    private String spiderUUID;

    private Date createTime;

    private Byte status;

    @JsonIgnore
    public StatusEnum getTimerTaskEnum() {
        return EnumUtils.getEnumByCode(timerTask, StatusEnum.class);
    }

    @JsonIgnore
    public StatusEnum getRunTaskEnum() {
        return EnumUtils.getEnumByCode(runTask, StatusEnum.class);
    }

}
