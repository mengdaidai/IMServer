package entity;

import java.nio.channels.SocketChannel;

public class ClientConnection {
	public SocketChannel channel;
	public String ip;
	public String token;
	
	public ClientConnection(SocketChannel channel, String ip,String token) {
		super();
		this.channel = channel;
		this.ip = ip;
		this.token = token;
	}
	public SocketChannel getChannel() {
		return channel;
	}
	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	
}
