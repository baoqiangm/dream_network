package m.dreamj.core.test;

import m.dreamj.core.network.ServerConfig;
import m.dreamj.core.network.http.HTTPServerHandler;

public class HSConnection extends HTTPServerHandler {
    private ServerConfig sc;

    public HSConnection(ServerConfig sc) {
        this.sc = sc;
    }

    @Override
    public void handle() {
        byte[] bs = new byte[this.content.readableBytes()];
        this.content.readBytes(bs);
        System.out.println("HSConnection.handle() >> " + new String(bs));
        send(sc.toString());
    }

}
