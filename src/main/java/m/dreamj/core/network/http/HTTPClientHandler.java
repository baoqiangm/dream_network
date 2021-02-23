package m.dreamj.core.network.http;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

public class HTTPClientHandler extends ChannelInboundHandlerAdapter {

    private final long outtime;
    private final TimeUnit outunit;
    private final ByteBuf content = Unpooled.buffer();
    private final ArrayBlockingQueue<String> contents = new ArrayBlockingQueue<>(1);

    private ChannelHandlerContext ctx;
    protected HttpRequest request;

    public HTTPClientHandler(long time, TimeUnit unit) {
        this.outtime = time;
        this.outunit = unit;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            this.ctx = ctx;
            return;
        }
        if (msg instanceof LastHttpContent) {
            return;
        }
        if (msg instanceof HttpContent) {
            content.writeBytes(((HttpContent) msg).content());
            return;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        handle();
        ctx.close();
    }

    private void handle() {
        byte[] bs = new byte[content.readableBytes()];
        content.readBytes(bs);
        try {
            contents.offer(new String(bs, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            error(-10, e.getMessage());
        }
    }

    public String getContent() {
        try {
            String content = contents.poll(outtime, outunit);
            if (ctx != null && ctx.channel().isOpen()) {
                ctx.close();
            }
            return content;
        } catch (InterruptedException e) {
            return "数据错误";
        }
    }

    private void error(int code, String msg) {
        contents.offer("数据错误");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        error(-1, cause.getMessage());
    }
}