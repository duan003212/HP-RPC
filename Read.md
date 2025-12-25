# 🚀 HP-RPC (High-Performance RPC Framework)

![Java](https://img.shields.io/badge/Java-17%2B-b07219)
![Netty](https://img.shields.io/badge/Netty-4.1-blue)
![Nacos](https://img.shields.io/badge/Nacos-2.3.0-green)
![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey)

**HP-RPC** 是一款基于 **Netty** + **Nacos** + **Kryo** 构建的轻量级、高性能分布式服务框架。

该项目旨在深入理解微服务底层通信原理，摒弃了传统的 HTTP 调用方式，通过自定义 TCP 私有协议、Netty 零拷贝技术以及高效的序列化手段，实现了低延迟、高吞吐的远程过程调用。

---

## 🏗️ 架构设计 (Architecture)

HP-RPC 采用了经典的 **Client-Server** 架构，配合注册中心实现服务治理：

```text
+----------------+      1. Register       +---------------+
| Service Server | <--------------------> | Nacos Registry|
+----------------+                        +---------------+
        ^                                         ^
        |                                         |
        | 2. TCP Connect (Netty)                  | 3. Discovery
        |                                         |
+----------------+                        +---------------+
| Service Client | ---------------------> |  Load Balancer|
+----------------+                        +---------------+
```

*   **Protocol**: 自定义 TCP 二进制协议（Magic + Version + Type + Length + Body）。
*   **Transport**: 基于 Netty 4.1 的 NIO Reactor 模型。
*   **Serialization**: 集成 Kryo 高性能序列化，体积比 Java 原生减少 80%。
*   **Registry**: 基于 Nacos 2.x 实现服务自动注册与发现。
*   **Proxy**: 使用 JDK 动态代理屏蔽网络通信细节。

---

## 🌟 核心亮点 (Features & Highlights)

*   **高性能网络通信**: 基于 **Netty** 主从多线程 Reactor 模型，利用 **ByteBuf 零拷贝** 和对象池技术，大幅降低 GC 压力与内存消耗。
*   **自定义通信协议**: 设计了应用层私有协议，通过自定义编解码器（Codec）完美解决了 TCP **粘包/拆包** 问题。
*   **异步转同步模型**: 利用 `CompletableFuture` 和 `ConcurrentHashMap` 设计了请求等待机制，实现了异步网络 IO 到同步业务调用的无缝转换。
*   **高效序列化**: 引入 **Kryo** 序列化框架，相比 Java 原生序列化性能提升 10 倍以上，显著降低网络带宽占用。
*   **服务自动治理**: 集成 **Nacos**，服务端启动自动注册，客户端自动发现，支持动态扩缩容。
*   **无感调用**: 实现了 Spring 风格的动态代理，客户端只需定义接口即可像调用本地方法一样发起远程请求。

---

## 🛠️ 技术栈 (Tech Stack)

| 组件 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **开发语言** | Java 17 | 使用新版 JDK 特性 |
| **网络框架** | Netty 4.1 | 异步事件驱动网络框架 |
| **注册中心** | Nacos 2.3.0 | 服务注册与发现 |
| **序列化** | Kryo 5.5 | 高性能 Java 序列化 |
| **构建工具** | Maven 3.8+ | 项目依赖管理 |
| **工具库** | Lombok, Slf4j | 简化代码与日志记录 |

---

## 📂 项目结构 (Project Structure)

```text
hp-rpc
├── hp-rpc-common       # 通用模块（实体对象、工具类）
├── hp-rpc-core         # 核心框架（Netty通信、序列化、注册中心、动态代理）
└── hp-rpc-test         # 测试模块（用于演示 Server 和 Client 的调用）
```

---

## 🚀 快速开始 (Quick Start)

### 1. 环境准备
*   JDK 17+
*   Maven 3.x
*   Nacos Server 2.x (运行在本地 8848 端口)

### 2. 启动 Nacos
确保本地 Nacos 已启动（单机模式）：
```bash
# Windows
startup.cmd -m standalone

# Linux/Mac
sh startup.sh -m standalone
```

### 3. 启动服务端 (Server)
运行 `hp-rpc-test` 模块下的 `TestServer.java`：
```java
// 代码示例
RpcServer server = new RpcServer(9999);
server.publishService(new UserServiceImpl(), UserService.class);
server.start();
```

### 4. 启动客户端 (Client)
运行 `hp-rpc-test` 模块下的 `TestClient.java`：
```java
// 代码示例
RpcClient client = new RpcClient();
UserService userService = client.createProxy(UserService.class);
String result = userService.sayHello("World");
System.out.println(result);
```
