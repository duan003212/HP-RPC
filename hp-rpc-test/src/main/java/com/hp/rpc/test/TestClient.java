package com.hp.rpc.test;

import com.hp.rpc.core.client.RpcClient;

public class TestClient {
    public static void main(String[] args) {

        RpcClient client = new RpcClient();

        UserService userService = client.createProxy(UserService.class);

        System.out.println("客户端准备发起调用...");
        for (int i = 0; i < 5; i++) {
            try {
                String result = userService.sayHello("面试官" + i);
                System.out.println("调用成功: " + result);
                Thread.sleep(1000); // 模拟间隔
            } catch (Exception e) {
                System.err.println("调用失败: " + e.getMessage());
            }
        }
    }
}