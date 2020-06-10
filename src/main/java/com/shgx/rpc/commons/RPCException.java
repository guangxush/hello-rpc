package com.shgx.rpc.commons;

/**
 * @author: guangxush
 * @create: 2020/06/10
 */
public class RPCException extends RuntimeException{
    public RPCException(String message, String s) {

    }

    public RPCException(String message) {
        super(message);
    }

    public RPCException(String message, Throwable e){
        super(message, e);
    }
}
