package com.via.light.rpc.exc;

/**
 * Created by stereo on 16-8-9.
 */
public class RpcException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -6865074239242615953L;
    private Throwable rootCause;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable rootCause) {
        super(message);

        this.rootCause = rootCause;
    }

    public RpcException(Throwable rootCause) {
        super(String.valueOf(rootCause));
        this.rootCause = rootCause;
    }

    public Throwable getRootCause() {
        return this.rootCause;
    }

    public Throwable getCause() {
        return getRootCause();
    }
}