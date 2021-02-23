package m.dreamj.core.network.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class TCPDispatcher extends ChannelInitializer<SocketChannel> {

    private List<TCPConnection> cs = new ArrayList<>();

    private Function<SocketChannel, ? extends TCPConnection> factory;

    public TCPDispatcher(String name, Function<SocketChannel, ? extends TCPConnection> factory) {
        this.factory = factory;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        TCPConnection connection = factory.apply(ch);

        pipeline.addLast("handler", connection);

        cs.add(connection);

        ch.closeFuture().addListener(listener -> {
            cs.remove(connection);
        });

    }

    public List<TCPConnection> getConnections() {
        return cs;
    }

    public int getConnectionSize() {
        return cs.size();
    }

    public void shutdown() {
        for (TCPConnection conn : cs) {
            conn.shutdown();
        }
    }
}
