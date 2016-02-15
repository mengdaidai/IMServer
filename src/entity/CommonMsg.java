package entity;

import java.nio.channels.SocketChannel;

import Server.MessageReceiver;

public class CommonMsg implements Cloneable{
	int off = 0;
	byte[] data = new byte[MessageReceiver.BUFFER_SIZE];
	int length = 0;
	byte type = 0;
	String from = "";
	String to = "";
	String token = "";
	byte[] time ;
	byte msg_id = 0;
	SocketChannel channel;
	public int getOff() {
		return off;
	}
	public void setOff(int off) {
		this.off = off;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int l) {
		this.length = l;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public byte[] getTime() {
		return time;
	}
	public void setTime(byte[] time) {
		this.time = time;
	}
	public SocketChannel getChannel() {
		return channel;
	}
	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}
	public byte getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(byte msg_id) {
		this.msg_id = msg_id;
	}
	
	
	  public Object clone () throws CloneNotSupportedException {
          CommonMsg o = null;
          o = (CommonMsg)super.clone();
          byte[] data = null;
         System.arraycopy(o.getData(), 0, data, 0, o.getData().length);
         o.setData(data);
         return o;
         }


}
