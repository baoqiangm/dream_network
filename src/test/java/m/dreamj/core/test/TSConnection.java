package m.dreamj.core.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;
import m.dreamj.core.network.ServerConfig;
import m.dreamj.core.network.tcp.TCPConnection;

public class TSConnection extends TCPConnection {
    private ServerConfig sc;

    public TSConnection(SocketChannel channel, ServerConfig sc) {
        super(channel);
        this.sc = sc;
    }

    @Override
    protected boolean processData(ByteBuf msg) {
        byte[] bs = new byte[msg.readableBytes()];
        msg.readBytes(bs);
        System.out.println(sc + " > " + new String(bs));
        return true;
    }

}
