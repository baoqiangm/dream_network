package m.dreamj.core.network.tcp;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import m.dreamj.core.network.ServerConfig;

public class TCPClient {

    private static Logger logger = LoggerFactory.getLogger(TCPClient.class);
    private final Bootstrap client;
    private final ConcurrentHashMap<String, TCPDispatcher> dispatchers;
    private ServerConfig sc;

    public TCPClient() {
        this(2);
    }

    public TCPClient(int coreSize) {
        client = new Bootstrap();
        dispatchers = new ConcurrentHashMap<>();
        client.group(new NioEventLoopGroup(coreSize));
        client.channel(NioSocketChannel.class);
    }

    public void connect(ServerConfig sc, Function<SocketChannel, ? extends TCPConnection> connection) {
        TCPDispatcher dispatcher = new TCPDispatcher(sc.name, connection);
        client.handler(dispatcher);
        ChannelFuture future = client.connect(new InetSocketAddress(sc.host, sc.port));

        future.addListener(listener -> {
            if (listener.isSuccess()) {
                logger.info("连接tcp服务器 [" + sc.name + ":" + sc.port + "] 服务器成功。 ");
            } else {
                logger.info("连接tcp服务器 [" + sc.name + ":" + sc.port + "] 服务器失败。失败原因： " + listener.cause().getMessage());
            }
        });

    }

    public void shutdown() {
        client.config().group().shutdownGracefully().addListener(listener -> {
            if (listener.isSuccess()) {
                logger.info("与tcp服务器 [" + sc.name + "] 断开连接成功！");
            } else {
                logger.info("与tcp服务器 [" + sc.name + "] 断开连接失败！" + listener.cause().getMessage());
            }
        });
        for (TCPDispatcher dispatcher : dispatchers.values()) {
            dispatcher.shutdown();
        }
        dispatchers.clear();
    }

}
