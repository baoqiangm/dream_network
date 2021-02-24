package m.dreamj.core.network;

/**
 * 服务器配置类
 * 
 * @author dreamj
 * @Date 2021-02-23 17:57
 */
public class ServerConfig {

    public final String name;
    public final String host;
    public final int    port;

    /**
     * @param name 服务器名称
     * @param host 服务器地址
     * @param port 服务器端口
     * @author dreamj
     * @Date 2021-02-23 17:58
     */
    public ServerConfig(String name, String host, int port) {
        super();
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ServerConfig [name=");
        builder.append(name);
        builder.append(", host=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append("]");
        return builder.toString();
    }

}
