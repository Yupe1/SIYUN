package com.yupe.siyun.util;

import java.util.HashMap;
import java.util.Map;

public class ResultData {
    private int errorCode;
    private String msg;
    private Map<String, Object> result;

    //增删改成功
    public static ResultData success(String msg) {
        ResultData res = new ResultData();
        res.msg = msg;
        res.errorCode = 0;
        return res;
    }
    //查成功
    public static ResultData success(String name, Object value, String msg) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(name, value);
        ResultData res = new ResultData();
        res.result = result;
        res.msg = msg;
        res.errorCode = 0;
        return res;
    }
    //查成功返回n个
    public static ResultData success(String[] names, Object[] values, String msg) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i < names.length; i++) {
            result.put(names[i], values[i]);
        }
        ResultData res = new ResultData();
        res.result = result;
        res.msg = msg;
        res.errorCode = 0;
        return res;
    }
    //错误
    public static ResultData error(int errorCode, String msg) {
        ResultData res = new ResultData();
        res.errorCode = errorCode;
        res.msg = msg;
        return res;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }
}
