package com.shgx.rpc.ptotocol;

import java.io.Serializable;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class Response implements Serializable {

    private String requestId;

    private Object result;

    private String exception;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "Response{" +
                "requestId='" + requestId + '\'' +
                ", result='" + result + '\'' +
                ", exception='" + exception + '\'' +
                '}';
    }
}
