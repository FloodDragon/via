package com.via.msgpack;

import org.msgpack.BeanMessage;

import java.lang.reflect.Method;

/**
 * Created by stereo on 16-8-5.
 */
public class UserInfo implements BeanMessage {

    public class A{}

    public long s = 100;

    public King king = new King();

    public Object object = new String("hellow");

    public RuntimeException ipcRuntimeException = new RuntimeException("test",new RuntimeException("不对"));

    public static void main(String[] args) {
        Method cls = UserInfo.class.getMethods()[0];
        System.out.println(cls.getDeclaringClass());
    }

    public class InnerClass1 {
        public InnerClass1() {
            System.out.println("Inner Class1");
        }
    }

}
