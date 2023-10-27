package com.geelink.connector.exception;

import com.geelink.connector.crawl.monitor.CodeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum ResultEnum implements CodeEnum {
    REQUEST_MISSING_PARAMETER(600, "API参数没有提供:"),
    REQUEST_INVALID_PARAMETER(601, "API参数错误:"),
    RESPONSE_NOT_FOUND(701, "结果未发现:"),

    QUARTZ_EXCEPTION(800, "任务管理错误："),
    QUARTZ_NOT_FOUND(801, "任务不存在："),

    JSON_CONVERT_ERROR(850, "转换数据错误"),
    TASK_NOT_EXIST(-100, "Task不存在!"),
    TASK_STATUS_ERROR(-101, "Task状态有误!"),
    SITE_NOT_EXIST(-102, "站点不存在!"),
    SUCCESS(1, "成功"),
    FAIL(-1, "失败"),
    BOOK_NOT_EXIST(-500, "作品不存在"),


    /** 格式校验  从-10001 开始 */
    MOBILE_ERROR(-1001, "手机号格式不正确"),
    MOBILE_CODE_FREQUENTLY(-1002, "验证码发送太过频繁，请稍后再试"),
    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
