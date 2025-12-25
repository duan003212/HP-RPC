package com.hp.rpc.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {
    private String requestId;
    private Integer code; // 200 成功, 500 失败
    private String message;
    private Object data;

    public static RpcResponse success(Object data, String requestId) {
        return RpcResponse.builder().code(200).data(data).requestId(requestId).build();
    }
    public static RpcResponse fail(String message, String requestId) {
        return RpcResponse.builder().code(500).message(message).requestId(requestId).build();
    }
}