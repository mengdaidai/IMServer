package entity;

public class TransmitionMessage {
	byte type = 0;
	String from = "";
	String time = "";
	byte[] data;
	byte msg_id;
	//还有一些属性需要补充
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
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public byte getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(byte msg_id) {
		this.msg_id = msg_id;
	}
	
}
