package com.shgx.rpc.ptotocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
@Data
public class Response implements Serializable {

    private String requestId;
    private Object result;
    private String exception;

    @Override
    public String toString() {
        return "Response{" +
                "requestId='" + requestId + '\'' +
                ", result='" + result + '\'' +
                ", exception='" + exception + '\'' +
                '}';
    }
}
