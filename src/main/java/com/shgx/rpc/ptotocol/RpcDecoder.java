package com.shgx.rpc.ptotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
public class RpcDecoder extends ByteToMessageDecoder{

    public RpcDecoder() {
    }

    @Override
    protected final void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out)throws Exception{
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object object = HessianSDK.deserialize(data);
        assert object != null;
        out.add(object);
    }
}
