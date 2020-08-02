package com.shgx.rpc.ptotocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
@Data
public class Request implements Serializable {
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private String serviceVersion;
}
