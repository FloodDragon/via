
package com.via.light.rpc.service;

/**
 * Created by LiuJing on 16-8-9.
 */
public class ServiceStateException extends RuntimeException {

    private static final long serialVersionUID = 1110000352259232646L;

    public ServiceStateException(String message) {
        super(message);
    }

    public ServiceStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceStateException(Throwable cause) {
        super(cause);
    }

    public static RuntimeException convert(Throwable fault) {
        if (fault instanceof RuntimeException) {
            return (RuntimeException) fault;
        } else {
            return new ServiceStateException(fault);
        }
    }

    public static RuntimeException convert(String text, Throwable fault) {
        if (fault instanceof RuntimeException) {
            return (RuntimeException) fault;
        } else {
            return new ServiceStateException(text, fault);
        }
    }
}
