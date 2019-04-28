package com.via.scan;

import com.via.rpc.conf.annotation.Service;
import com.via.rpc.server.api.IService;
import com.via.rpc.server.skeleton.service.ServiceProxyFactory;

@Service(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello() {
        return "Hello world!";
    }


    public static void main(String[] args) throws Exception {
        IService iService = ServiceProxyFactory.createServiceProxyInstance(new HelloServiceImpl());
        System.out.println(iService.getServiceName());
    }
}
