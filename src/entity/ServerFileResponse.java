package entity;

public class ServerFileResponse {
	byte file_id;
	String ip;
	int port;
	boolean is_off;
	boolean yes;
	public byte getFile_id() {
		return file_id;
	}
	public void setFile_id(byte file_id) {
		this.file_id = file_id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isIs_off() {
		return is_off;
	}
	public void setIs_off(boolean is_off) {
		this.is_off = is_off;
	}
	public boolean isYes() {
		return yes;
	}
	public void setYes(boolean yes) {
		this.yes = yes;
	}
	
	
}
