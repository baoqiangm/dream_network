package m.dreamj.core.network.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;

public abstract class HTTPServerHandler extends ChannelInboundHandlerAdapter {
    protected ChannelHandlerContext ctx;
    protected HttpRequest request;

    protected Map<String, List<String>> querys;
    protected ByteBuf content;
    private HttpMethod method;

    public HTTPServerHandler() {
        this(1024 * 4);
    }

    public HTTPServerHandler(int capSize) {
        super();
        this.content = Unpooled.buffer(capSize);
    }

    public HTTPServerHandler(HttpMethod method) {
        this(1024 * 4, method);
    }

    public HTTPServerHandler(int capSize, HttpMethod method) {
        super();
        this.content = Unpooled.buffer(capSize);
        this.method = method;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            if (null != method && request.method() != method) {
                exceptionCaught(ctx, new RuntimeException("请求方法不支持"));
            }
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            querys = decoder.parameters();
            return;
        }
        if (msg instanceof LastHttpContent) {
            content.writeBytes(((LastHttpContent) msg).content());
            return;
        }
        if (msg instanceof HttpContent) {
            content.writeBytes(((HttpContent) msg).content());
            return;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        handle();

        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ctx = null;
        querys = null;
        content = null;
        request = null;
    }

    public abstract void handle();

    public void send(String data) {
        send(data.getBytes(Charset.forName("utf-8")));
    }

    public void send(byte[] datas) {
        ByteBuf buff = Unpooled.directBuffer();
        buff.writeBytes(datas);
        send(buff);
    }

    public void send(ByteBuf buff) {
        if (ctx == null) {
            return;
        }
        FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.content().writeBytes(buff);
        ctx.writeAndFlush(response);
        ctx.close();
        ctx = null;
        querys = null;
    }

    protected void shutdown() {

    }
}
