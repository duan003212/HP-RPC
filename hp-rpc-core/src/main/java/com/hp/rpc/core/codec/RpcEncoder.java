package com.hp.rpc.core.codec;

import com.hp.rpc.core.serializer.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<Object> {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        byte[] bytes = KryoSerializer.serialize(msg);
        out.writeInt(MAGIC_NUMBER);
        out.writeInt(1);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}