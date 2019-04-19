package com.via.rpc.server.skeleton.service;

import com.via.rpc.server.api.INotification;
import com.via.rpc.server.api.IService;
import com.via.rpc.server.api.ISkeletonContext;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * 服务基类(可相互广播)
 *
 * @author LiuJing
 */
public abstract class Service implements IService {

    protected String serviceName = "serviceName";
    protected ISkeletonContext actionContext;

    @Override
    public void onRegister() {
    }

    @Override
    public void onRemove() {
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public Service(Class<?> cls) {
        this.serviceName = cls.getName();
    }

    @Override
    public IService resolveService(String actionName) {
        if (this.serviceName.equals(actionName))
            return this;
        return null;
    }

    @Override
    public INotification sendNotification(String notificationName, Object body,
                                          String type) {
        INotification notification = new Notification(notificationName, body,
                type);
        actionContext.notifyObservers(notification);
        return notification;
    }

    @Override
    public INotification sendNotification(String notificationName, Object body) {
        return sendNotification(notificationName, body, null);
    }

    @Override
    public INotification sendNotification(String notificationName) {
        return sendNotification(notificationName, null, null);
    }

    @Override
    public void setServiceContext(ISkeletonContext actionContext) {
        this.actionContext = actionContext;
    }

    public final static class ServiceCodeTemplate {

        public final static String CODE_TEMPLATE;

        static {
            StringBuilder code = new StringBuilder();
            code.append("package {};\n");
            code.append("import {};\n");
            code.append("import java.lang.*;\n");
            code.append("import com.via.rpc.server.api.IService;\n");
            code.append("import com.via.rpc.server.api.ISkeletonContext;\n");
            code.append("import com.via.rpc.server.api.INotification;\n");
            code.append("import com.via.rpc.server.skeleton.service.Notification;\n");
            code.append("public class {} extends {} implements IService {\n");
            code.append("protected String serviceName=null;\n");
            code.append("protected ISkeletonContext actionContext=null;\n");
            code.append("public {}() {\n");
            code.append("this.serviceName=\"{}\";\n");
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
            CODE_TEMPLATE = code.toString();
        }

        public final static String buildCodeTemplate(String serviceClassPackage,
                                                     String serviceClassFullName,
                                                     String serviceProxyName,
                                                     String serviceClassName,
                                                     String serviceInterfaceClassName) {
            Object[] argArray = {
                    serviceClassPackage,
                    serviceClassFullName,
                    serviceProxyName,
                    serviceClassName,
                    serviceProxyName,
                    serviceInterfaceClassName
            };
            FormattingTuple ft = MessageFormatter.arrayFormat(CODE_TEMPLATE, argArray);
            return ft.getMessage();
        }
    }
}