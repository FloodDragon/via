package com.via.light.rpc.utils;

import com.via.light.rpc.server.api.IServiceCall;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工具类
 *
 * @author stereo-liujing
 * @version 2013.12.21
 **/
public class InvokeUtils {

    private static final Logger log = LoggerFactory
        .getLogger(InvokeUtils.class);

    private static final Object[] nullReturn = new Object[] {null, null};

    /**
     * 找到精确的方法
     *
     * @param service
     * @param methodName
     * @param args
     * @return
     */
    public static Object[] findMethodWithExactParameters(Object service,
        String methodName, List<?> args) {
        Object[] arguments = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            arguments[i] = args.get(i);
        }
        return findMethodWithExactParameters(service, methodName, arguments);
    }

    /**
     * @param service
     * @param methodName
     * @param args
     * @return
     */
    public static Object[] findMethodWithExactParameters(Object service,
        String methodName, Object[] args) {
        int numParams = (args == null) ? 0 : args.length;

        Method method = null;

        // 自动反射匹配业务
        try {
            method = service.getClass().getMethod(methodName,
                ConversionUtils.convertParams(args));
            //log.debug("精确找到的方法：", methodName);
            return new Object[] {method, args};
        } catch (NoSuchMethodException nsme) {
            //log.debug("未找到方法使用精确的参数类型");
        }

        // 手动反射匹配业务

        List<Method> methods = ConversionUtils.findMethodsByNameAndNumParams(
            service, methodName, numParams);

        if (methods.isEmpty()) {
            return new Object[] {null, null};
        } else if (methods.size() == 1 && args == null) {
            return new Object[] {methods.get(0), null};
        } else if (methods.size() > 1) {
            log.debug("发现多个方法具有相同的名称/参数.");
            log.debug("Parameter conversion will be attempted in order.");
        }

        Object[] params = null;

        for (int i = 0; i < methods.size(); i++) {
            method = methods.get(i);
            boolean valid = true;
            Class<?>[] paramTypes = method.getParameterTypes();
            for (int j = 0; j < args.length; j++) {
                if ((args[j] == null && paramTypes[j].isPrimitive())
                    || (args[j] != null && !args[j].getClass().equals(
                    paramTypes[j]))) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                return new Object[] {method, args};
            }
        }

        for (int i = 0; i < methods.size(); i++) {
            try {
                method = methods.get(i);
                params = ConversionUtils.convertParams(args,
                    method.getParameterTypes());
                if (args.length > 0 && (args[0] instanceof IServiceCall)
                    && (!(params[0] instanceof IServiceCall))) {
                    continue;
                }
                return new Object[] {method, params};
            } catch (Exception ex) {
                log.debug("转换失败 ：", method);
            }
        }

        return new Object[] {null, null};
    }

    /**
     * 找到关于集合的方法
     *
     * @param service
     * @param methodName
     * @param args
     * @return
     */
    public static Object[] findMethodWithListParameters(Object service,
        String methodName, List<?> args) {
        Object[] arguments = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            arguments[i] = args.get(i);
        }

        return findMethodWithListParameters(service, methodName, arguments);
    }

    public static Object[] findMethodWithListParameters(Object service,
        String methodName, Object[] args) {

        Method method = null;

        try {
            method = service.getClass().getMethod(methodName,
                ConversionUtils.convertParams(args));
            log.debug("精确找到方法:", methodName);
            return new Object[] {method, args};
        } catch (NoSuchMethodException nsme) {
            log.debug("未找到方法");
        }

        List<Method> methods = ConversionUtils.findMethodsByNameAndNumParams(
            service, methodName, 1);
        log.debug("Found {} methods", methods.size());
        if (methods.isEmpty()) {
            return new Object[] {null, null};
        } else if (methods.size() > 1) {
            log.debug("发现多个方法具有相同的名称/参数");
            log.debug("Parameter conversion will be attempted in order.");
        }

        ArrayList<Object> argsList = new ArrayList<Object>();
        if (args != null) {
            for (Object element : args) {
                argsList.add(element);
            }
        }
        args = new Object[] {argsList};

        Object[] params = null;
        for (int i = 0; i < methods.size(); i++) {
            try {
                method = methods.get(i);
                params = ConversionUtils.convertParams(args,
                    method.getParameterTypes());
                if (argsList.size() > 0
                    && (argsList.get(0) instanceof IServiceCall)
                    && (!(params[0] instanceof IServiceCall))) {
                    continue;
                }
                return new Object[] {method, params};
            } catch (Exception ex) {
                log.debug("转换失败 ：", ex);
            }
        }
        return nullReturn;
    }
}