package com.via.rpc.conf;

import com.via.rpc.client.FailoverHandler;
import com.via.rpc.server.listener.ClientHeartbeatBodyListener;
import com.via.rpc.server.listener.ClientLiveExpiredListener;
import com.via.rpc.server.listener.ClientRegisterListener;
import com.via.rpc.server.listener.ClientUnregisterListener;
import com.via.rpc.utils.Constants;
import com.via.rpc.utils.NetUtils;

import java.net.InetSocketAddress;

public final class Config {
    private int soLinger = -1;
    private int timeout = 5 * 1000;
    private int connectTimeout = 5 * 1000;
    private int sendTimeout = 5 * 1000;
    private int readTimeout = 15 * 1000;
    private int idleTimeout = 30 * 1000;
    private int heartBeatQuantity = 5;
    private int heartBeatRate = 5 * 1000;// 5s
    private int heartBeatExpireInterval = 60 * 1000; // 60s
    private boolean ssl = false;
    private boolean tcpNoDelay = true;
    private boolean reuseAddress = true;
    private int sendBufferSize = 65535;
    private int receiveBufferSize = 65535;
    private boolean useEpoll = false;
    private int childNioEventThreads = 6; //cpu+1
    private int payload = 8 * 1024 * 1024;
    private InetSocketAddress remoteAddress = new InetSocketAddress(
        "0.0.0.0", 8099);
    private int businessPoolSize = 200;//业务处理线程
    private String businessPoolType = Constants.THREADPOOL_TYPE_CACHED;//线程池类型
    private String businessPoolQueueType = Constants.QUEUE_TYPE_NORMAL;  // 队列类型
    private int businessPoolQueueSize = 0; // 队列大小
    private int connectAccepts;

    //客户端设置故障转移处理
    private FailoverHandler failoverHandler;
    //服务器端的监听处理(客户端无需设置)
    private ClientRegisterListener clientRegisterListener;
    private ClientUnregisterListener clientUnregisterListener;
    private ClientLiveExpiredListener clientLiveExpiredListener;
    private ClientHeartbeatBodyListener clientHeartbeatBodyListener;

    public Config() {
        this(8099);
    }

    public Config(int port) {
        this("0.0.0.0", port);
    }

    public Config(InetSocketAddress address) {
        this.remoteAddress = address;
    }

    public Config(String hostname, int port) {
        if (NetUtils.isInvalidPort(port) || NetUtils.isInvalidHost(hostname)) {
            throw new RuntimeException("hostname:" + hostname + " or port:" + port + " is Invalid");
        }
        remoteAddress = new InetSocketAddress(hostname, port);
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isSsl() {
        return ssl;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public int getSoLinger() {
        return soLinger;
    }

    public void setSoLinger(int soLinger) {
        this.soLinger = soLinger;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress address) {
        this.remoteAddress = address;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getHeartBeatRate() {
        return heartBeatRate;
    }

    public void setHeartBeatRate(int heartBeatRate) {
        this.heartBeatRate = heartBeatRate;
    }

    public int getHeartBeatExpireInterval() {
        return heartBeatExpireInterval;
    }

    public void setHeartBeatExpireInterval(int heartBeatExpireInterval) {
        this.heartBeatExpireInterval = heartBeatExpireInterval;
    }

    public String getHost() {
        return remoteAddress != null ? remoteAddress.getHostName() : null;
    }

    public int getPort() {
        return remoteAddress != null ? remoteAddress.getPort() : -1;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public int getChildNioEventThreads() {
        return childNioEventThreads;
    }

    public void setChildNioEventThreads(int childNioEventThreads) {
        this.childNioEventThreads = childNioEventThreads;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public int getPayload() {
        return payload;
    }

    public void setPayload(int payload) {
        this.payload = payload;
    }

    public int getBusinessPoolSize() {
        return businessPoolSize;
    }

    public String getBusinessPoolType() {
        return businessPoolType;
    }

    public String getBusinessPoolQueueType() {
        return businessPoolQueueType;
    }

    public int getBusinessPoolQueueSize() {
        return businessPoolQueueSize;
    }

    public void setBusinessPoolSize(int businessPoolSize) {
        this.businessPoolSize = businessPoolSize;
    }

    public void setBusinessPoolType(String businessPoolType) {
        this.businessPoolType = businessPoolType;
    }

    public void setBusinessPoolQueueType(String businessPoolQueueType) {
        this.businessPoolQueueType = businessPoolQueueType;
    }

    public void setBusinessPoolQueueSize(int businessPoolQueueSize) {
        this.businessPoolQueueSize = businessPoolQueueSize;
    }

    public int getHeartBeatQuantity() {
        return heartBeatQuantity;
    }

    public void setHeartBeatQuantity(int heartBeatQuantity) {
        this.heartBeatQuantity = heartBeatQuantity;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getConnectAccepts() {
        return connectAccepts;
    }

    public void setConnectAccepts(int connectAccepts) {
        this.connectAccepts = connectAccepts;
    }

    public ClientRegisterListener getClientRegisterListener() {
        return clientRegisterListener;
    }

    public void setClientRegisterListener(ClientRegisterListener clientRegisterListener) {
        this.clientRegisterListener = clientRegisterListener;
    }

    public ClientUnregisterListener getClientUnregisterListener() {
        return clientUnregisterListener;
    }

    public void setClientUnregisterListener(
        ClientUnregisterListener clientUnregisterListener) {
        this.clientUnregisterListener = clientUnregisterListener;
    }

    public ClientLiveExpiredListener getClientLiveExpiredListener() {
        return clientLiveExpiredListener;
    }

    public void setClientLiveExpiredListener(
        ClientLiveExpiredListener clientLiveExpiredListener) {
        this.clientLiveExpiredListener = clientLiveExpiredListener;
    }

    public ClientHeartbeatBodyListener getClientHeartbeatBodyListener() {
        return clientHeartbeatBodyListener;
    }

    public void setClientHeartbeatBodyListener(
        ClientHeartbeatBodyListener clientHeartbeatBodyListener) {
        this.clientHeartbeatBodyListener = clientHeartbeatBodyListener;
    }

    public FailoverHandler getFailoverHandler() {
        return failoverHandler;
    }

    public void setFailoverHandler(FailoverHandler failoverHandler) {
        this.failoverHandler = failoverHandler;
    }

    @Override
    public String toString() {
        return "Config{" +
            "soLinger=" + soLinger +
            ", timeout=" + timeout +
            ", connectTimeout=" + connectTimeout +
            ", sendTimeout=" + sendTimeout +
            ", readTimeout=" + readTimeout +
            ", idleTimeout=" + idleTimeout +
            ", heartBeatQuantity=" + heartBeatQuantity +
            ", heartBeatRate=" + heartBeatRate +
            ", heartBeatExpireInterval=" + heartBeatExpireInterval +
            ", ssl=" + ssl +
            ", tcpNoDelay=" + tcpNoDelay +
            ", reuseAddress=" + reuseAddress +
            ", sendBufferSize=" + sendBufferSize +
            ", receiveBufferSize=" + receiveBufferSize +
            ", useEpoll=" + useEpoll +
            ", childNioEventThreads=" + childNioEventThreads +
            ", payload=" + payload +
            ", remoteAddress=" + remoteAddress +
            ", businessPoolSize=" + businessPoolSize +
            ", businessPoolType='" + businessPoolType + '\'' +
            ", businessPoolQueueType='" + businessPoolQueueType + '\'' +
            ", businessPoolQueueSize=" + businessPoolQueueSize +
            ", connectAccepts=" + connectAccepts +
            ", failoverHandler=" + failoverHandler +
            ", clientRegisterListener=" + clientRegisterListener +
            ", clientUnregisterListener=" + clientUnregisterListener +
            ", clientLiveExpiredListener=" + clientLiveExpiredListener +
            ", clientHeartbeatBodyListener=" + clientHeartbeatBodyListener +
            '}';
    }
}