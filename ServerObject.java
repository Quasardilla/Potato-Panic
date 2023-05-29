public class ServerObject {
    protected String name;
    protected String ip;
    protected short port;

    public ServerObject(String name, String ip, short port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public short getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(short port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServerItem [name=" + name + ", ip=" + ip + ", port=" + port + "]";
    }
}
