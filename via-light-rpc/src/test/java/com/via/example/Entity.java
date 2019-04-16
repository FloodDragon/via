package com.via.example;

import org.msgpack.BeanMessage;

/**
 * Created by liuj-ai on 2019/1/22.
 */
public class Entity implements BeanMessage {
    private String xxx;

    public String getXxx() {
        return xxx;
    }

    public void setXxx(String xxx) {
        this.xxx = xxx;
    }
}
