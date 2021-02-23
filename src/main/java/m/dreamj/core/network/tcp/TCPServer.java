package m.dreamj.core.network.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import m.dreamj.core.network.ServerConfig;

public class TCPServer {

    private final static Logger logger = LoggerFactory.getLogger(TCPServer.class);
    private final ConcurrentHashMap<String, TCPDispatcher> dispatchers;

    private final ServerBootstrap server;

    public TCPServer() {
        this(1, 2);
    }

    public TCPServer(int acceptSize, int coreSize) {
        this.dispatchers = new ConcurrentHashMap<>();
        this.server = new ServerBootstrap();
        int backlog = 1024;
        server.group(new NioEventLoopGroup(acceptSize), new NioEventLoopGroup(coreSize)).channel(NioServerSocketChannel.class);
        server.option(ChannelOption.SO_BACKLOG, backlog).option(ChannelOption.SO_REUSEADDR, Boolean.valueOf(true));
        server.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000).childOption(ChannelOption.TCP_NODELAY, true);
        server.childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        server.handler(new LoggingHandler(LogLevel.DEBUG));
    }

    public void bind(ServerConfig sc, Function<SocketChannel, ? extends TCPConnection> connection) throws InterruptedException {

        TCPDispatcher dispatcher = new TCPDispatcher(sc.name, connection);
        server.childHandler(dispatcher);
        ChannelFuture f = server.bind(sc.port).sync();
        f.addListener(listener -> {
            if (listener.isSuccess()) {
                logger.info("tcp服务器 [" + sc.name + "] 监听端口 [" + sc.port + "] 启动成功。");
                dispatchers.put(sc.name, dispatcher);
            } else {
                logger.info("tcp服务器 [" + sc.name + "] 监听端口 [" + sc.port + "] 失败. 失败原因： " + listener.cause().getMessage());
                System.exit(0);
            }
        });
    }

    public void close(String name) {
        dispatchers.remove(name);
        if (server != null) {
            server.config().childGroup().shutdownGracefully();
            server.config().group().shutdownGracefully().addListener(listener -> {
                if (listener.isSuccess()) {
                    logger.info("tcp服务器 [" + name + "] 关闭成功！");
                } else {
                    logger.info("tcp服务器 [" + name + "] 关闭失败！" + listener.cause().getMessage());
                }
            });
        }
    }

    public void shutdown() {
        for (String name : dispatchers.keySet()) {
            close(name);
        }
    }

    public List<TCPConnection> getConnections(String name) {
        TCPDispatcher dispatcher = dispatchers.get(name);
        if (dispatcher != null) {
            return dispatcher.getConnections();
        }
        return new ArrayList<>();
    }

    public int getConnectionSize(String name) {
        TCPDispatcher dispatcher = dispatchers.get(name);
        if (dispatcher != null) {
            return dispatcher.getConnectionSize();
        }
        return 0;

    }

}
