package entity;

import java.util.ArrayList;
import java.util.List;

public class LoginResponseMessage {
	String token = "";
	ArrayList<TransmitionMessage> msgs;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public ArrayList<TransmitionMessage> getMsgs() {
		return msgs;
	}
	public void setMsgs(ArrayList<TransmitionMessage> msgs) {
		this.msgs = msgs;
	}
}
