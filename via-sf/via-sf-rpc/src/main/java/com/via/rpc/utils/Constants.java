package com.via.rpc.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiuJing on 16-8-9.
 */
public final class Constants {

    public final static String QUEUE_TYPE_NORMAL = "normal";
    public final static String QUEUE_TYPE_PRIORITY = "priority";

    public final static String THREADPOOL_TYPE_FIXED = "fixed";
    public final static String THREADPOOL_TYPE_CACHED = "cached";

    public final static String CHANNEL_CLIENT_ID = "channel_client_id";

    /**
     * packet state
     */
    public static final byte STATUS_PENDING = 0x01; // 未处理
    public static final byte STATUS_SUCCESS_RESULT = 0x02; // 调用成功并返回结果
    public static final byte STATUS_SUCCESS_NULL = 0x03; // 调用成功并返回NULL
    public static final byte STATUS_SUCCESS_VOID = 0x04; // 调用成功无结果
    public static final byte STATUS_SERVICE_NOT_FOUND = 0x10;// 业务未找到
    public static final byte STATUS_METHOD_NOT_FOUND = 0x11; // 方法未找到
    public static final byte STATUS_ACCESS_DENIED = 0x12; // 拒绝访问
    public static final byte STATUS_INVOCATION_EXCEPTION = 0x13;// 调用时异常
    public static final byte STATUS_GENERAL_EXCEPTION = 0x14; // 一般异常
    public static final byte STATUS_APP_SHUTTING_DOWN = 0x15; // 应用程序关闭
    public static final byte STATUS_NOT_CONNECTED = 0x20; // 未连接

    /**
     * packet type
     */
    public static final byte TYPE_REQUEST = 0x01;	//普通请求类型
    public static final byte TYPE_RESPONSE = 0x02;	//普通响应类型

    /**
     * heartbeat type
     */
    public static final byte TYPE_HEARTBEAT_REQUEST_REGISTER = 0x03;//心跳注册
    public static final byte TYPE_HEARTBEAT_REQUEST_UNREGISTER = 0x04;//心跳注销
    public static final byte TYPE_HEARTBEAT = 0x05; //心跳请求类型

    public static final Map<Class,String> primitiveClassMap = new HashMap<Class, String>();
    static {
        primitiveClassMap.put(int.class,"0");
        primitiveClassMap.put(Integer.class,"0");

        primitiveClassMap.put(long.class,"1");
        primitiveClassMap.put(Long.class,"1");

        primitiveClassMap.put(short.class,"2");
        primitiveClassMap.put(Short.class,"2");

        primitiveClassMap.put(byte.class,"3");
        primitiveClassMap.put(Byte.class,"3");


        primitiveClassMap.put(char.class,"4");
        primitiveClassMap.put(Character.class,"4");

        primitiveClassMap.put(boolean.class,"5");
        primitiveClassMap.put(Boolean.class,"5");

        primitiveClassMap.put(float.class,"6");
        primitiveClassMap.put(Float.class,"6");

        primitiveClassMap.put(double.class,"7");
        primitiveClassMap.put(Double.class,"7");

        //primitiveClassMap.put(void.class,"8");
        //primitiveClassMap.put(Void.class,"8");
    }
}
