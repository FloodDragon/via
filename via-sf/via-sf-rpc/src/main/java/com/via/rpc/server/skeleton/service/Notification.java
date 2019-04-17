package com.via.rpc.server.skeleton.service;

import com.via.rpc.server.api.INotification;

public class Notification implements INotification {

    protected String name = null, type = null;

    protected Object body = null, result = null;

    public Notification(String name, Object body, String type) {
        this.name = name;
        this.body = body;
        this.type = type;
    }

    public Notification(String name) {
        this.name = name;
        body = null;
        type = null;
    }

    public Notification(String name, Object body) {
        this.name = name;
        this.body = body;
        type = null;
    }

    public Object getBody() {
        return body;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String toString() {
        String result = "Notification Name: " + getName() + " Body:";
        if (body != null)
            result += body.toString() + " Type:";
        else
            result += "null Type:";

        if (type != null)
            result += type;
        else
            result += "null ";

        return result;
    }
}