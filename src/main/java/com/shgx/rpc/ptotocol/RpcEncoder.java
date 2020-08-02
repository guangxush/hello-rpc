package com.shgx.rpc.ptotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
public class RpcEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf){
        byte[] data = HessianSDK.serialize(o);
        assert  data != null;
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
