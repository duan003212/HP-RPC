package com.hp.rpc.core.server;

import com.hp.rpc.common.entity.RpcRequest;
import com.hp.rpc.common.entity.RpcResponse;
import com.hp.rpc.core.codec.RpcDecoder;
import com.hp.rpc.core.codec.RpcEncoder;
import com.hp.rpc.core.registry.NacosRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcServer {
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();


    private final int port;

    public RpcServer(int port) {
        this.port = port;
    }

    public void publishService(Object service, Class<?> serviceClass) {
        serviceMap.put(serviceClass.getName(), service);
        try {
            NacosRegistry.registerService(serviceClass.getName(), new InetSocketAddress("127.0.0.1", this.port));
            log.info("Service registered: {} on port {}", serviceClass.getName(), this.port);
        } catch (Exception e) {
            log.error("Register service failed", e);
        }
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new SimpleChannelInboundHandler<RpcRequest>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
                                            RpcResponse response;
                                            try {
                                                Object service = serviceMap.get(request.getInterfaceName());
                                                Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
                                                Object result = method.invoke(service, request.getParameters());
                                                response = RpcResponse.success(result, request.getRequestId());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                response = RpcResponse.fail(e.getMessage(), request.getRequestId());
                                            }
                                            ctx.writeAndFlush(response);
                                        }
                                    });
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("HP-RPC Server started on port {}", port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}