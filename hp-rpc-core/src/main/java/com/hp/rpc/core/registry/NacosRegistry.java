package com.hp.rpc.core.registry;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import java.net.InetSocketAddress;

public class NacosRegistry {
    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static NamingService namingService;

    static {
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerService(String serviceName, InetSocketAddress address) {
        try {
            namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
        } catch (Exception e) {
            throw new RuntimeException("Register service failed", e);
        }
    }

    public static InetSocketAddress lookupService(String serviceName) {
        try {
            var instances = namingService.getAllInstances(serviceName);
            if (instances.isEmpty()) throw new RuntimeException("No service available: " + serviceName);
            Instance instance = instances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (Exception e) {
            throw new RuntimeException("Lookup service failed", e);
        }
    }
}