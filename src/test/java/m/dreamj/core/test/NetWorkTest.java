package m.dreamj.core.test;

import org.junit.Test;

import m.dreamj.core.network.ServerConfig;
import m.dreamj.core.network.http.HTTPClient;
import m.dreamj.core.network.http.HTTPClientHandler;
import m.dreamj.core.network.http.HTTPServer;
import m.dreamj.core.network.tcp.TCPClient;
import m.dreamj.core.network.tcp.TCPServer;

public class NetWorkTest {

    @Test
    public void TestTCPServer() {
        ServerConfig sc1 = new ServerConfig("test1", "127.0.0.1", 8011);
        ServerConfig sc2 = new ServerConfig("test2", "127.0.0.1", 8012);
        ServerConfig sc3 = new ServerConfig("test3", "127.0.0.1", 8013);
        TCPServer s1 = new TCPServer();
        try {
            s1.bind(sc1, (ch) -> new TSConnection(ch, sc1));
            s1.bind(sc2, (ch) -> new TSConnection(ch, sc2));
            s1.bind(sc3, (ch) -> new TSConnection(ch, sc3));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new TCPClient().connect(sc1, (ch) -> new TCConnection(ch, sc1));
        new TCPClient().connect(sc2, (ch) -> new TCConnection(ch, sc2));
        new TCPClient().connect(sc3, (ch) -> new TCConnection(ch, sc3));
        new TCPClient().connect(sc3, (ch) -> new TCConnection(ch, sc3));
        new TCPClient().connect(sc1, (ch) -> new TCConnection(ch, sc1));
        // 等待通讯完成
        try {
            Thread.sleep(3_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void TestHTTPServer() throws Exception {
        HTTPServer s1 = new HTTPServer();
        HTTPServer s2 = new HTTPServer();
        HTTPServer s3 = new HTTPServer();

        ServerConfig sc1 = new ServerConfig("test1", "127.0.0.1", 8011);
        ServerConfig sc2 = new ServerConfig("test2", "127.0.0.1", 8012);
        ServerConfig sc3 = new ServerConfig("test3", "127.0.0.1", 8013);
        s1.bind(sc1, () -> new HSConnection(sc1));
        s2.bind(sc2, () -> new HSConnection(sc2));
        s3.bind(sc3, () -> new HSConnection(sc3));

        HTTPClient c = new HTTPClient();

        c.connect("http://127.0.0.1:8011/test");

        for (int i = 0; i < 10; i++) {
            final int x = i;
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(3_000);
                        for (int j = 0; j < 10; j++) {
                            HTTPClientHandler conn = c.send("test .. " + Thread.currentThread().getName() + " >> " + (x * 1000 + j));
                            String s = conn.getContent();
                            System.out.println("NetWorkTest.TestHTTPServer() > " + s);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }, "run-" + i).start();
        }
        // 等待通讯完成
        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
