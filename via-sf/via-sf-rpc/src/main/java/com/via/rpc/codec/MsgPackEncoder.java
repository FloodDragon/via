package com.via.rpc.codec;

import com.via.rpc.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * Created by LiuJing on 16-8-4.
 */
public class MsgPackEncoder extends MessageToByteEncoder<Packet> {

    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    private final MessagePack messagePack = new MessagePack();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();
        ByteBufOutputStream bout = new ByteBufOutputStream(out);
        bout.write(LENGTH_PLACEHOLDER);
        byte[] data = messagePack.write(packet);
        bout.write(data);
        bout.flush();
        bout.close();
        int endIdx = out.writerIndex();
        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
