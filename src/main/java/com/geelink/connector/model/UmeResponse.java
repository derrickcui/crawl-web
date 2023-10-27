package com.geelink.connector.model;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class UmeResponse {
    private int status;
    private String message;
    private UmeCode umeCode;
    private Object result;

    private UmeResponse(){}

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UmeCode getUmeCode() {
        return umeCode;
    }

    public void setUmeCode(UmeCode umeCode) {
        this.umeCode = umeCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public static class Builder {
        private int status;
        private String message;
        private UmeCode umeCode;
        private Object result;

        public Builder withStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withUmeCode(UmeCode umeCode) {
            this.umeCode = umeCode;
            return this;
        }

        public Builder withResult(Object result) {
            this.result = result;
            return this;
        }

        public UmeResponse build() {
            UmeResponse response = new UmeResponse();
            response.status = this.status;
            response.message = this.message;
            response.umeCode = this.umeCode;
            response.result = this.result;
            return response;
        }
    }
}
