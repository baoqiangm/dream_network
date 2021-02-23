package m.dreamj.core.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;
import m.dreamj.core.network.ServerConfig;
import m.dreamj.core.network.tcp.TCPConnection;

public class TCConnection extends TCPConnection {
    private ServerConfig sc;

    public TCConnection(SocketChannel channel, ServerConfig sc) {
        super(channel);
        this.sc = sc;
    }

    @Override
    public void connected() {
        send("sc > " + sc);
    }

    @Override
    protected boolean processData(ByteBuf msg) {
        return true;
    }

}
