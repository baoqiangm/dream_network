package m.dreamj.core.network;

public class ServerConfig {

    public final String name;
    public final String host;
    public final int port;

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
