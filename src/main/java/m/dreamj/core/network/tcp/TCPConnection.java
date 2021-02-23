package m.dreamj.core.network.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

public abstract class TCPConnection extends ChannelInboundHandlerAdapter {

    private final static Logger log = LoggerFactory.getLogger(TCPConnection.class);
    private final SocketChannel channel;

    private ByteBuf readBuffer = Unpooled.directBuffer(2048);

    public TCPConnection(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        connected();
    }

    /**
     * 连接成功处理
     */
    public void connected() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        if (buffer.capacity() > readBuffer.writableBytes()) {
            log.error("接收数据包 [" + buffer.capacity() + "] 大于可缓存数据包 [" + readBuffer.writableBytes() + "] 小异常，连接关闭!");
            close();
            return;
        }
        readBuffer.writeBytes(buffer);

        while (readBuffer.readableBytes() > 4 && readBuffer.readableBytes() - 4 >= readBuffer.getInt(readBuffer.readerIndex())) {
            if (!this.parse(readBuffer)) {
                close();
                return;
            }
        }
        if (readBuffer.isReadable()) {
            readBuffer.discardReadBytes();
        } else {
            readBuffer.clear();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("远程连接 [" + getRemoteAddr() + "] 异常 ： [ " + cause.getMessage() + "] 连接关闭！");
        close();
        ctx.close();
    }

    public String getRemoteAddr() {
        return channel.remoteAddress().toString();
    }

    public void shutdown() {
        channel.close();
        close();

    }

    public void close() {
    }

    private boolean parse(ByteBuf buffer) {
        int length = 0;
        try {
            length = buffer.readInt();
            ByteBuf _buffer = buffer.slice();
            _buffer.writerIndex(length);
            buffer.readerIndex(buffer.readerIndex() + length);
            return processData(_buffer);
        } catch (Exception e) {
            log.warn("处理数据包" + buffer + "包长:" + length, e);
            return false;
        }
    }

    public void send(String data) {
        ByteBuf buff = Unpooled.directBuffer();
        buff.writeInt(data.getBytes().length);
        buff.writeBytes(data.getBytes());
        channel.writeAndFlush(buff);
    }

    protected abstract boolean processData(ByteBuf msg) throws Exception;

}
