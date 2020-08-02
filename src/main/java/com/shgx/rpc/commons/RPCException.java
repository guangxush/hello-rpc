package com.shgx.rpc.commons;

/**
 * @author: guangxush
 * @create: 2020/06/10
 */
public class RPCException extends RuntimeException {
    private String exceptionCode;
    private String exceptionMessage;

    public RPCException(String exceptionCode, String exceptionMessage) {
        this.exceptionCode = exceptionCode;
        this.exceptionMessage = exceptionMessage;
    }

    public RPCException(String message) {
        super(message);
    }

    public RPCException(String message, Throwable e) {
        super(message, e);
    }
}
