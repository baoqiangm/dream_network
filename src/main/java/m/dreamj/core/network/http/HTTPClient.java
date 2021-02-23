package m.dreamj.core.network.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public class HTTPClient {

    private final ConcurrentHashMap<Channel, HTTPClientHandler> connections = new ConcurrentHashMap<>();

    private Bootstrap b = new Bootstrap();
    private URI uri;
    private int outtime = 5;
    private TimeUnit outunit = TimeUnit.SECONDS;

    public HTTPClient() {
        this(5);
    }

    public HTTPClient(int coreThreadSize) {
        this(2, 5);
    }

    public HTTPClient(String url) throws URISyntaxException {
        this(2, url, 5);
    }

    public HTTPClient(int coreThreadSize, String url, int timeout) throws URISyntaxException {
        this(coreThreadSize, timeout);
        connect(url);
    }

    public HTTPClient(int coreThreadSize, int timeout) {
        this.outtime = timeout;
        EventLoopGroup group = new NioEventLoopGroup(coreThreadSize);
        b.group(group).channel(NioSocketChannel.class);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, outtime * 1000);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new HttpClientCodec());
                HTTPClientHandler conn = new HTTPClientHandler(outtime, outunit);
                p.addLast(conn);
                connections.put(ch, conn);
            }
        });

    }

    public void connect(String url) throws URISyntaxException {
        uri = new URI(url);
    }

    /**
     * 消息返回请求超时时间，单位 秒
     * 
     * @param time
     */
    public void timeout(int time) {
        this.outtime = time;
    }

    public HTTPClientHandler send(String content) throws InterruptedException {
        Channel ch = b.connect(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 80).sync().channel();
        HTTPClientHandler conn = connections.remove(ch);
        if (conn == null) {
            throw new RuntimeException("connection is error :" + uri);
        }

        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath(), Unpooled.copiedBuffer(content.getBytes()));
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.getBytes().length);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);

        ChannelFuture future = ch.writeAndFlush(request);

        future.addListener(listener -> {
            if (!listener.isSuccess()) {
                conn.exceptionCaught(null, new RuntimeException("write data is error :" + uri));
            }
        });
        return conn;

    }

    public void shutdown() {
        b.config().group().shutdownGracefully().addListener(listener -> {
            b = null;
        });

    }

}
