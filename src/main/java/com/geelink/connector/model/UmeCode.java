package com.geelink.connector.model;

public enum UmeCode {
    SUCCESS("0000000", "Success"),
    ERROR("E000000", "Error"),
    UNKNOWN_EXCEPTION("UN00000", "Unknown ERROR");
    private String code;
    private String message;

    UmeCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code(){
        return this.code;
    }

    public String message() {
        return this.message;
    }
}
