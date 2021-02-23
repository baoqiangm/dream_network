package m.dreamj.core.network.http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import m.dreamj.core.network.ServerConfig;

public class HTTPServer {

    private static Logger logger = LoggerFactory.getLogger(HTTPServer.class);
    private final ConcurrentHashMap<String, HTTPDispatcher> dispatchers;

    private final ServerBootstrap server;

    public HTTPServer() {
        this(1, 2);
    }

    public HTTPServer(int acceptSize, int coreSize) {
        this.dispatchers = new ConcurrentHashMap<>();
        this.server = new ServerBootstrap();
        int backlog = 1024;
        server.group(new NioEventLoopGroup(acceptSize), new NioEventLoopGroup(coreSize)).channel(NioServerSocketChannel.class);
        server.option(ChannelOption.SO_BACKLOG, backlog).option(ChannelOption.SO_REUSEADDR, Boolean.valueOf(true));
        server.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000).childOption(ChannelOption.TCP_NODELAY, true);
        server.childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        server.handler(new LoggingHandler(LogLevel.DEBUG));
    }

    public void bind(ServerConfig sc, Supplier<HTTPServerHandler> connection) throws InterruptedException {

        HTTPDispatcher dispatcher = new HTTPDispatcher(sc.name, connection);
        server.childHandler(dispatcher);
        ChannelFuture f = server.bind(sc.port).sync();
        f.addListener(listener -> {
            if (listener.isSuccess()) {
                logger.info("http服务器 [" + sc.name + "] 监听端口 [" + sc.port + "] 启动成功。");
                dispatchers.put(sc.name, dispatcher);
            } else {
                logger.info("http服务器 [" + sc.name + "] 监听端口 [" + sc.port + "] 失败. 失败原因： " + listener.cause().getMessage());
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
                    logger.info("http服务器 [" + name + "] 关闭成功！");
                } else {
                    logger.info("http服务器 [" + name + "] 关闭失败！" + listener.cause().getMessage());
                }
            });
        }
    }

    public void shutdown() {
        for (String name : dispatchers.keySet()) {
            close(name);
        }
    }

    public List<HTTPServerHandler> getConnections(String name) {
        HTTPDispatcher dispatcher = dispatchers.get(name);
        if (dispatcher != null) {
            return dispatcher.getConnections();
        }
        return new ArrayList<>();
    }

    public int getConnectionSize(String name) {
        HTTPDispatcher dispatcher = dispatchers.get(name);
        if (dispatcher != null) {
            return dispatcher.getConnectionSize();
        }
        return 0;

    }

}
