package com.via.light.rpc.protocol;

import com.via.light.rpc.utils.Time;
import org.msgpack.BeanMessage;

/**
 * Created by LiuJing on 16-8-15.
 */
public class Heartbeat implements BeanMessage {
    private String client_id;
    private long client_time;
    private long server_time;
    private String body;

    public Heartbeat() {
    }

    public Heartbeat(String client_id) {
        this.client_id = client_id;
        now();
    }

    public void now() {
        client_time = Time.now();
    }

    public String getClient_id() {
        return client_id;
    }

    public long getClient_time() {
        return client_time;
    }

    public long getServer_time() {
        return server_time;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public void setClient_time(long client_time) {
        this.client_time = client_time;
    }

    public void setServer_time(long server_time) {
        this.server_time = server_time;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Heartbeat heartbeat = (Heartbeat) o;
        return client_id.equals(heartbeat.client_id);
    }

    @Override
    public int hashCode() {
        return client_id.hashCode();
    }

    @Override public String toString() {
        return "Heartbeat{" +
            "client_id='" + client_id + '\'' +
            ", client_time=" + client_time +
            ", server_time=" + server_time +
            ", body='" + body + '\'' +
            '}';
    }
}
