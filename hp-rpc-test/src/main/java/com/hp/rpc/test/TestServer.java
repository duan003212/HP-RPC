package com.hp.rpc.test;

import com.hp.rpc.core.server.RpcServer;

public class TestServer {
    public static void main(String[] args) {

        RpcServer server = new RpcServer(9999);

        UserService userService = new UserServiceImpl();
        server.publishService(userService, UserService.class);

        System.out.println("正在启动服务端...");
        server.start();
    }
}