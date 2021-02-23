package m.dreamj.core.network.http;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HTTPDispatcher extends ChannelInitializer<SocketChannel> {

    private List<HTTPServerHandler> cs = new ArrayList<>();

    private Supplier<HTTPServerHandler> factory;

    public HTTPDispatcher(String name, Supplier<HTTPServerHandler> factory) {
        this.factory = factory;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new CorsHandler(corsConfig));

        HTTPServerHandler connection = factory.get();

        pipeline.addLast("handler", connection);

        cs.add(connection);

        ch.closeFuture().addListener(listener -> {
            cs.remove(connection);
        });

    }

    public List<HTTPServerHandler> getConnections() {
        return cs;
    }

    public int getConnectionSize() {
        return cs.size();
    }

    public void shutdown() {
        for (HTTPServerHandler conn : cs) {
            conn.shutdown();
        }
    }
}
