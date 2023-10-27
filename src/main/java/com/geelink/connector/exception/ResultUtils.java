package com.geelink.connector.exception;

import org.springframework.util.StringUtils;

public class ResultUtils {

    public static ResultVO succss(Object object) {
        ResultVO vo = new ResultVO();
        vo.setCode(ResultEnum.SUCCESS.getCode());
        vo.setMessage(ResultEnum.SUCCESS.getMessage());
        vo.setData(object);
        return vo;
    }

    public static ResultVO error(String msg) {
        ResultVO vo = new ResultVO();
        vo.setCode(ResultEnum.FAIL.getCode());
        vo.setMessage(StringUtils.isEmpty(msg) ? ResultEnum.FAIL.getMessage() : msg);
        return vo;
    }

    public static ResultVO error(Integer code, String msg) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(code);
        resultVO.setMessage(msg);
        return resultVO;
    }

}
