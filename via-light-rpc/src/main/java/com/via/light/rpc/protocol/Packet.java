package com.via.light.rpc.protocol;

import com.via.light.rpc.utils.Constants;
import com.via.light.rpc.utils.UUID;
import java.util.Arrays;

import org.msgpack.BeanMessage;

/**
 * 信息包
 *
 * @author LiuJing
 * @version 2017.1.17
 */
public final class Packet implements BeanMessage {
    private static final long serialVersionUID = -3447224470014044569L;

    private String id;

    private byte type;

    private byte state;

    private String interfaceName;

    private String method;

    private Object[] params;

    private Object result;

    private Class<?> returnType;

    private Heartbeat heartbeat;

    private String exception;

    public Packet() {
    }

    public Packet(Object result) {
        this.result = result;
    }

    public Packet(String interfaceName, String method, Object[] params) {
        super();
        this.interfaceName = interfaceName;
        this.method = method;
        this.params = params;
    }

    public Packet(String id, byte type, byte state, String interfaceName, String method, Object[] params) {
        super();
        this.id = id;
        this.type = type;
        this.state = state;
        this.interfaceName = interfaceName;
        this.method = method;
        this.params = params;
    }

    public Packet(String id, byte type, byte state, String interfaceName, String method, Object[] params,
        Class<?> returnType) {
        super();
        this.id = id;
        this.type = type;
        this.state = state;
        this.interfaceName = interfaceName;
        this.method = method;
        this.params = params;
        this.returnType = returnType;
    }

    public Packet(String id, byte type, byte state, Heartbeat heartbeat) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.heartbeat = heartbeat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
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

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Heartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    @Override
    public String toString() {
        return "Packet{" +
            "id='" + id + '\'' +
            ", type=" + type +
            ", state=" + state +
            ", interfaceName='" + interfaceName + '\'' +
            ", method='" + method + '\'' +
            ", params=" + Arrays.toString(params) +
            ", result=" + result +
            ", returnType=" + returnType +
            ", heartbeat=" + heartbeat +
            ", exception='" + exception + '\'' +
            '}';
    }

    public static Packet packetRequest(String serviceName, String method,
        Class<?> returnType, Object[] params) {
        UUID uuid = new UUID(serviceName + "-" + method);
        return new Packet(uuid.toString(), Constants.TYPE_REQUEST, Constants.STATUS_PENDING, serviceName, method, params, returnType);
    }

    public static Packet packetHeartBeat(Heartbeat heartbeat, byte type) {
        UUID uuid = new UUID(heartbeat.getClient_id());
        return new Packet(uuid.toString(), type, Constants.STATUS_PENDING, heartbeat);
    }
}