package com.via.scan;

import com.via.common.compiler.support.JavassistCompiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello() {
        return "Hello world!";
    }

    /**
     * @Version 1.0
     * 用于在线扫描
     * 测试在线编译进行扫描注解进行实例化类
     */
    public static String testOnlineCompilerCode(HelloService helloService) {
        Class<?> cls = helloService.getClass();
        Class<?> interfaceCls = cls.getInterfaces()[0];
        StringBuilder code = new StringBuilder();
        code.append("package ").append(cls.getPackage().getName()).append(";").append("\n");
        code.append("import java.lang.*;\n");
        code.append("import " + cls.getName() + ";\n");
        code.append("import com.via.rpc.server.api.IService;\n");
        code.append("import com.via.rpc.server.api.ISkeletonContext;\n");
        code.append("import com.via.rpc.server.api.INotification;\n");
        code.append("import com.via.rpc.server.skeleton.service.Notification;\n");
        code.append("public class " + cls.getSimpleName() + "_0 extends " + cls.getSimpleName() + " implements IService { \n");
        code.append("protected String serviceName;\n");
        code.append("protected ISkeletonContext actionContext;\n");
        code.append("public " + cls.getSimpleName() + "_0(){\n");
        code.append("this.serviceName=\"" + interfaceCls.getSimpleName() + "\";\n");
        code.append("}\n");

        code.append("@Override\n" +
                "public void onRegister() {\n" +
                "}\n");

        code.append("@Override\n" +
                "public void onRemove() {\n" +
                "}\n");

        code.append("@Override\n" +
                "public String getServiceName() {\n" +
                "   return serviceName;\n" +
                "}\n");

        code.append("@Override\n" +
                "public IService resolveService(String actionName) {\n" +
                "   if (this.serviceName.equals(actionName))\n" +
                "   return this;\n" +
                "return null;\n" +
                "}\n");

        code.append("@Override\n" +
                "public INotification sendNotification(String notificationName, Object body,\n" +
                "                                          String type) {\n" +
                "   INotification notification = new Notification(notificationName, body, type);\n" +
                "   actionContext.notifyObservers(notification);\n" +
                "   return notification;\n" +
                "}\n");

        code.append("@Override\n" +
                "public INotification sendNotification(String notificationName, Object body) {\n" +
                "   return sendNotification(notificationName, body, null);\n" +
                "}\n");

        code.append("@Override\n" +
                "public INotification sendNotification(String notificationName) {\n" +
                "   return sendNotification(notificationName, null, null);\n" +
                "}\n");

        code.append("@Override\n" +
                "public void setServiceContext(ISkeletonContext actionContext) {\n" +
                "   this.actionContext = actionContext;\n" +
                "}\n");

        code.append("@Override\n" +
                "public void handleNotification(INotification notification) {\n" +
                "}\n");

        code.append("}\n");
        return code.toString();
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String code = testOnlineCompilerCode(new HelloServiceImpl());
        JavassistCompiler compiler = new JavassistCompiler();
        Class<?> clazz = compiler.compile(code, JavassistCompiler.class.getClassLoader());
        Object instance = clazz.newInstance();
        Method sayHello = instance.getClass().getMethod("sayHello");
        System.out.println(sayHello.invoke(instance));
    }
}
