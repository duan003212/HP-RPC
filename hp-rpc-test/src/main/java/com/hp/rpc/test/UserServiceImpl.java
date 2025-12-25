package com.hp.rpc.test;

public class UserServiceImpl implements UserService {
    @Override
    public String sayHello(String name) {
        return "你好 " + name + ", 这是来自 HP-RPC 的响应 (Time: " + System.currentTimeMillis() + ")";
    }
}