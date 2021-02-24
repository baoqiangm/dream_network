package m.dreamj.core.network.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 服务连接派发器
 * 
 * @author dreamj
 * @Date 2021-02-23 18:02
 */
public class TCPDispatcher extends ChannelInitializer<SocketChannel> {

    private List<TCPConnection>                              cs = new ArrayList<>();

    private Function<SocketChannel, ? extends TCPConnection> factory;

    public TCPDispatcher(String name, Function<SocketChannel, ? extends TCPConnection> factory) {
        this.factory = factory;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline   = ch.pipeline();

        TCPConnection   connection = factory.apply(ch);

        pipeline.addLast("handler", connection);

        cs.add(connection);

        ch.closeFuture().addListener(listener -> {
            cs.remove(connection);
        });

    }

    /**
     * 获取当前派发器中存在的所有连接
     * 
     * @return
     * @author dreamj
     * @Date 2021-02-23 18:02
     */
    public List<TCPConnection> getConnections() {
        return cs;
    }

    /**
     * 获取当前派发器中存在的所有连接数
     * 
     * @return
     * @author dreamj
     * @Date 2021-02-23 18:03
     */
    public int getConnectionSize() {
        return cs.size();
    }

    /**
     * 关闭所有连接
     * 
     * @author dreamj
     * @Date 2021-02-23 18:03
     */
    public void shutdown() {
        for (TCPConnection conn : cs) {
            conn.shutdown();
        }
    }
}
