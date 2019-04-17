package com.via.rpc.server.skeleton.service;

import com.via.rpc.exc.MethodNotFoundException;
import com.via.rpc.exc.NotAllowedException;
import com.via.rpc.server.api.IServiceCall;
import com.via.rpc.server.api.IServiceInvoker;
import com.via.rpc.server.api.ISkeletonContext;
import com.via.rpc.utils.Constants;
import com.via.rpc.utils.ConversionUtils;
import com.via.rpc.utils.InvokeUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.management.ServiceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service 调用器
 *
 * @author LiuJing
 * @version 2013.12.21 首版
 * @version 2019.1.22  MethodWrapper 支持javassist 代理service提高效率(需要改进)
 */
public final class ServiceInvoker implements IServiceInvoker {

    private static Logger LOG = LoggerFactory.getLogger(ServiceInvoker.class);

    private static class MethodWrapper {
        private String methodName;
        private Class<?>[] classes;
        private Method method;

        public MethodWrapper(String methodName, Class<?>[] classes, Method method) {
            this.methodName = methodName;
            this.classes = classes;
            this.method = method;
        }

        public Method findMethod(String methodName, Object[] args) {
            if (this.methodName.equals(methodName)
                && args.length == classes.length) {
                for (int i = 0; i < args.length; i++)
                    if (!args[i].getClass().isAssignableFrom(classes[i]))
                        return null;
                return method;
            }
            return null;
        }
    }

    private ISkeletonContext skeletonContext;
    private Set<MethodWrapper> methodCaches = new CopyOnWriteArraySet<MethodWrapper>();

    public ServiceInvoker(ISkeletonContext skeletonContext) {
        this.skeletonContext = skeletonContext;
    }

    @Override
    public Object getService(String serviceName) {
        return skeletonContext.retrieveService(serviceName);
    }

    /**
     * 调用ServiceCall
     *
     * @param call
     * @return
     */
    @Override
    public boolean invoke(IServiceCall call) {
        return invoke(call, getService(call.getInterfaceName()));
    }

    /**
     * 调用带业务的ServiceCall
     *
     * @param call
     * @param service
     * @return
     */
    @Override
    public boolean invoke(IServiceCall call, Object service) {
        if (service == null) {
            String interfaceName = call.getInterfaceName();
            call.setException(new ServiceNotFoundException(interfaceName));
            call.setStatus(Constants.STATUS_SERVICE_NOT_FOUND);
            LOG.warn("skeleton not found: {}", interfaceName);
            return false;
        } else {
            String methodName = call.getMethodName();
            if (methodName.charAt(0) == '@') {
                methodName = methodName.substring(1);
            }
            Object[] args = call.getArguments();
            Object[] argsWithCall;
            if (args != null) {
                argsWithCall = new Object[args.length + 1];
                argsWithCall[0] = call;
                for (int i = 0; i < args.length; i++) {
                    argsWithCall[i + 1] = args[i];
                }
            } else {
                argsWithCall = new Object[] {call};
            }
            Object[] methodResult = null;
            if (methodCaches.size() > 0) {
                Method method = null;
                for (MethodWrapper cache : methodCaches) {
                    if (cache.findMethod(methodName, argsWithCall) != null) {
                        method = cache.findMethod(methodName, argsWithCall);
                        break;
                    }
                }
                if (method != null) {
                    methodResult = new Object[] {method, argsWithCall};
                }
            }
            if (methodResult == null) {
                methodResult = InvokeUtils.findMethodWithExactParameters(
                    service, methodName, argsWithCall);
                if (methodResult.length == 0 || methodResult[0] == null) {
                    methodResult = InvokeUtils.findMethodWithExactParameters(
                        service, methodName, args);
                    if (methodResult.length == 0 || methodResult[0] == null) {
                        methodResult = InvokeUtils
                            .findMethodWithListParameters(service,
                                methodName, argsWithCall);
                        if (methodResult.length == 0 || methodResult[0] == null) {
                            methodResult = InvokeUtils
                                .findMethodWithListParameters(service,
                                    methodName, args);
                            if (methodResult.length == 0
                                || methodResult[0] == null) {
                                LOG.error(
                                    "没有找到匹配参数的Method",
                                    new Object[] {
                                        methodName,
                                        (args == null ? Collections.EMPTY_LIST
                                            : Arrays.asList(args)),
                                        service});
                                call.setStatus(Constants.STATUS_METHOD_NOT_FOUND);
                                if (args != null && args.length > 0) {
                                    call.setException(new MethodNotFoundException(
                                        methodName, args));
                                } else {
                                    call.setException(new MethodNotFoundException(
                                        methodName));
                                }
                                return false;
                            }
                        }
                    }
                }
            }

            Object result = null;
            Method method = (Method) methodResult[0];
            Object[] params = (Object[]) methodResult[1];
            try {
                //LOG.debug("Invoking method: ", method.toString());
                // if (method.getReturnType() != call.getReturnType()) {
                // call.setStatus(IServiceCall.STATUS_METHOD_NOT_FOUND);
                // call.setException(new MethodNotFoundException(methodName
                // + " not match "));
                // return false;
                // }
                if (method.getReturnType() == Void.class) {
                    method.invoke(service, params);
                    call.setStatus(Constants.STATUS_SUCCESS_VOID);
                } else {
                    result = method.invoke(service, params);
                   // LOG.debug("result: {}", result);
                    call.setStatus(result == null ? Constants.STATUS_SUCCESS_NULL
                        : Constants.STATUS_SUCCESS_RESULT);
                }
                call.setResult(result);
                methodCaches.add(new MethodWrapper(methodName, ConversionUtils
                    .convertParams(params), method));

            } catch (NotAllowedException e) {
                call.setException(e);
                call.setStatus(Constants.STATUS_ACCESS_DENIED);
                return false;
            } catch (IllegalAccessException accessEx) {
                call.setException(accessEx);
                call.setStatus(Constants.STATUS_ACCESS_DENIED);
                LOG.error("Error executing call:", call);
                LOG.error("Service invocation error", accessEx);
                return false;
            } catch (InvocationTargetException invocationEx) {
                call.setException(invocationEx);
                call.setStatus(Constants.STATUS_INVOCATION_EXCEPTION);
                return false;
            } catch (Exception ex) {
                call.setException(ex);
                call.setStatus(Constants.STATUS_GENERAL_EXCEPTION);
                LOG.error("Error executing call: ", call);
                LOG.error("Service invocation error", ex);
                return false;
            }
            return true;
        }
    }
}