package io.github.solam.util.emums;

import lombok.Getter;

@Getter
public enum TaskRunStateEnum {

    TASK_RUNNING("running", "任务运行中"),
    TASK_STOP("stop", "任务结束");

    private final String code;

    private final String name;

    TaskRunStateEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
