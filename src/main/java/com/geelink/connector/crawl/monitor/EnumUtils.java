package com.geelink.connector.crawl.monitor;

public class EnumUtils {

    public static <T extends CodeEnum> T getEnumByCode(Integer code, Class<T> enumClass) {
        for (T each :enumClass.getEnumConstants()){
            if (code.equals(each.getCode())) {
                return each;
            }
        }
        return null;
    }
}
