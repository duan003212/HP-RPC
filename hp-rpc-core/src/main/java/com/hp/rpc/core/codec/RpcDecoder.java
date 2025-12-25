package com.hp.rpc.core.codec;

import com.hp.rpc.core.serializer.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    private final Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        if (in.readableBytes() < 12) return;

        in.markReaderIndex();
        int magic = in.readInt();
        int version = in.readInt();
        int length = in.readInt();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object obj = KryoSerializer.deserialize(bytes, genericClass);
        out.add(obj);
    }
}