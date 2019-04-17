package com.via.light.rpc.server.skeleton.service;

import com.via.light.rpc.protocol.Packet;
import com.via.light.rpc.server.api.ICallback;
import com.via.light.rpc.server.api.IServiceCall;
import com.via.light.rpc.utils.Constants;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LiuJing
 */
public class ServiceCall implements IServiceCall {

    protected String id; // ID

    protected String interfaceName; // 业务名

    protected String methodName; // 方法名

    protected Object[] arguments = null; // 参数

    protected byte status = Constants.STATUS_PENDING;// 状态

    private Object result; // 返回值

    private Class<?> returnType;// 返回值类型

    private Exception exception; // 返回的异常

    protected Packet packet; // 处理的数据包

    private HashSet<ICallback> callbacks = new HashSet<ICallback>(); // 回调接口(可选)

    public ServiceCall() {
    }

    public ServiceCall(Packet packet) {
        this.packet = packet;
        this.id = packet.getId();
        this.status = packet.getState();
        this.interfaceName = packet.getInterfaceName();
        this.methodName = packet.getMethod();
        this.arguments = packet.getParams();
        this.returnType = packet.getReturnType();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isSuccess() {
        return (status == Constants.STATUS_SUCCESS_RESULT)
            || (status == Constants.STATUS_SUCCESS_NULL)
            || (status == Constants.STATUS_SUCCESS_VOID);
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public Object getResultPacket() {
        return packet;
    }

    @Override
    public void setResult(Object result) {
        this.result = result;
        packet.setState(status);
        packet.setType(Constants.TYPE_RESPONSE);
        if (isSuccess()) {
            packet.setResult(result);
        }
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public String getInterfaceName() {
        return this.interfaceName;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public byte getStatus() {
        return status;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public Set<ICallback> getCallbacks() {
        return null;
    }

    @Override
    public void registerCallback(ICallback callback) {
    }

    @Override
    public void setException(Exception exception) {
        this.exception = exception;
        packet.setException(exception.toString());
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }
}