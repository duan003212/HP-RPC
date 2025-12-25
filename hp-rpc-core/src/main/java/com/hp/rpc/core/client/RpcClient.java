package com.hp.rpc.core.client;

import com.hp.rpc.common.entity.RpcRequest;
import com.hp.rpc.common.entity.RpcResponse;
import com.hp.rpc.core.codec.RpcDecoder;
import com.hp.rpc.core.codec.RpcEncoder;
import com.hp.rpc.core.registry.NacosRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClient {
    private static final Map<String, CompletableFuture<RpcResponse>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest request = RpcRequest.builder()
                            .requestId(UUID.randomUUID().toString())
                            .interfaceName(interfaceClass.getName())
                            .methodName(method.getName())
                            .parameters(args)
                            .paramTypes(method.getParameterTypes())
                            .build();

                    InetSocketAddress address = NacosRegistry.lookupService(interfaceClass.getName());

                    CompletableFuture<RpcResponse> future = new CompletableFuture<>();
                    PENDING_REQUESTS.put(request.getRequestId(), future);

                    sendRequest(address, request);

                    RpcResponse response = future.get();
                    if (response.getCode() != 200) {
                        throw new RuntimeException(response.getMessage());
                    }
                    return response.getData();
                });
    }

    private void sendRequest(InetSocketAddress address, RpcRequest request) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(new SimpleChannelInboundHandler<RpcResponse>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
                                            CompletableFuture<RpcResponse> future = PENDING_REQUESTS.remove(response.getRequestId());
                                            if (future != null) {
                                                future.complete(response);
                                            }
                                            ctx.close();
                                        }
                                    });
                        }
                    });
            ChannelFuture future = bootstrap.connect(address).sync();
            future.channel().writeAndFlush(request).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Send request failed", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}