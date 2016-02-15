package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.codec.digest.DigestUtils;

import DB.DBHelper;

import com.google.gson.Gson;

import entity.ClientConnection;
import entity.CommonMsg;
import entity.FileEntity;
import entity.FriendMsg;
import entity.LoginMessage;
import entity.LoginResponseMessage;
import entity.RegisterMessage;
import entity.ServerFileResponse;
import entity.TransmitionMessage;

public class MessageHandler {
	ThreadPoolExecutor executor ;
	private final int ID_START = 10000000;
	MessageWriter writer;
	HashMap<String,ClientConnection> clients;
	DBHelper helper;
	
	
	public MessageHandler(){
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
		writer = new MessageWriter();
		clients = new HashMap<String,ClientConnection>();
		helper = new DBHelper();
		new Thread(writer).start();
	}
	
	public void handleMsg(CommonMsg msg){
		System.out.println("handleMsg!");
		executor.execute(new HandleThread(msg));
		
	}
	
	
	class HandleThread implements Runnable{
		CommonMsg msg;
		
		public HandleThread(CommonMsg msg){
			this.msg = msg;
		}

		@Override
		public void run() {
			System.out.println("处理头！");
			handleHead(msg);
			boolean isValid = handleToken(msg);
			if(isValid){
				System.out.println("处理token!");
				switch(msg.getType()){
				
				case Protocol.REGISTER_TYPE://注册消息
					handleRegister(msg);
					break;
				case Protocol.LOGIN_TYPE://登录信息
					handleLogin(msg);
					break;
				case Protocol.TEXT_TYPE:
					handleTransmition(msg);
					break;
				case Protocol.VOICE_TYPE:
					handleTransmition(msg);
					break;
				case Protocol.PICTURE_TYPE:
					handleTransmition(msg);
					break;
				case Protocol.FILE_TYPE:
					handleFile(msg);
					break;
				case Protocol.FILE_RESPONSE_TYPE:
					handleFileResponse(msg);
					break;
				case Protocol.TRANSMITION_RESPONSE_TYPE:
					handleTransmitionResponse(msg);
					break;
				case Protocol.FRIEND_TYPE:
					handleFriend(msg);
					break;
				case Protocol.FRIEND_RESPONSE_TYPE:
					handleFriendResponse(msg);
					break;
				case Protocol.HEARTBEAT_TYPE:
					handleHeartBeat(msg);
					break;
				
			}
			
			}
		}
		
	}
	
	private void handleHead(CommonMsg msg){
		byte[] data = msg.getData();
		byte type_byte = data[Protocol.TYPE_START];
		msg.setType(type_byte);
		byte[] msg_id_byte = Arrays.copyOfRange(data, Protocol.MESSAGE_ID_START, Protocol.FROM_START);
		byte msg_id = msg_id_byte[0];
		msg.setMsg_id(msg_id);
		byte[] from_byte = Arrays.copyOfRange(data, Protocol.FROM_START, Protocol.TOKEN_START);
		String from = new String(from_byte);
		msg.setFrom(from);
		byte[] token_byte = Arrays.copyOfRange(data, Protocol.TOKEN_START, Protocol.TO_START);
		String token = new String(token_byte);
		msg.setToken(token);
		byte[] to_byte = Arrays.copyOfRange(data, Protocol.TO_START, Protocol.TIME_START);
		String to = new String(to_byte);
		msg.setTo(to);
		byte[] time_byte = Arrays.copyOfRange(data, Protocol.TIME_START, Protocol.HEAD_LENGTH);
		msg.setTime(time_byte);
		msg.setData(Arrays.copyOfRange(data, Protocol.HEAD_LENGTH, msg.getLength()));
		System.out.println("消息类型："+msg.getType());
	}
	
	private void handleLogin(CommonMsg msg){
		LoginMessage login_msg = new Gson().fromJson(new String(msg.getData()),LoginMessage.class);
		if(helper.login(msg.getFrom(), login_msg.getPassword())){
			System.out.println("login success!");
			String token = DigestUtils.md5Hex(msg.getFrom()+login_msg.getPassword()+msg.getTime());
			LoginResponseMessage res_msg = new LoginResponseMessage();
			res_msg.setToken(token);
			ArrayList<TransmitionMessage> trans_msg = helper.getOfflineMsg(msg.getFrom());
			System.out.println("trans_msg:"+trans_msg.size());
			res_msg.setMsgs(trans_msg);
			String remote_address = msg.getChannel().socket().getRemoteSocketAddress().toString();
			clients.put(msg.getFrom(), new ClientConnection(msg.getChannel(),remote_address.substring(1, remote_address.indexOf(':')),token));			
			String temp = msg.getFrom();
			msg.setFrom(msg.getTo());
			msg.setTo(temp);
			msg.setType(Protocol.LOGIN_RESPONSE_TYPE);
			System.out.println(new Gson().toJson(res_msg));
			msg.setData(new Gson().toJson(res_msg).getBytes());
			writer.registerWriteChannel(msg.getChannel(), msg);
		}else{
			String temp = msg.getFrom();
			msg.setFrom(msg.getTo());
			msg.setTo(temp);
			msg.setType(Protocol.LOGIN_RESPONSE_TYPE);
			String response = "用户名或密码错误！";
			msg.setData(response.getBytes());
			writer.registerWriteChannel(msg.getChannel(), msg);
		}
		
		//System.out.println(login_msg.getPassword());
		//System.out.println(token);
		//System.out.println(token.getBytes().length);
	}
	
	private void handleRegister(CommonMsg msg){
		System.out.println(new String(msg.getData()));
		RegisterMessage register_msg = new Gson().fromJson(new String(msg.getData()),RegisterMessage.class);
		System.out.println("开始注册！");
		String user_id = helper.register( register_msg.getUser_name(), register_msg.getPassword());
		String from_temp = msg.getFrom();
		msg.setFrom(msg.getTo());
		msg.setTo(from_temp);
		String response = user_id;
		msg.setData(response.getBytes());
		msg.setType(Protocol.REGISTER_RESPONSE_TYPE);
		writer.registerWriteChannel(msg.getChannel(), msg);
		//writer.registerWriteChannel(msg.getChannel(), msg);
		//System.out.println(register_msg.getUser_name());
		//System.out.println(register_msg.getPassword());
	}

	private void handleTransmition(CommonMsg msg){
		ClientConnection connection = clients.get(msg.getTo());
		if(connection == null){
			helper.insertMsg(msg.getType(), msg.getFrom(), msg.getTo(),msg.getData());
			
		}else{
			//helper.insertMsg(msg.getType(), msg.getFrom(), msg.getTo(), msg.getData());
			writer.registerWriteChannel(connection.getChannel(), msg);
			
		}
		System.out.println("收到文本消息！");
		
	}
	
	

	private void handleHeartBeat(CommonMsg msg){
		String temp = msg.getFrom();
		msg.setFrom(msg.getTo());
		msg.setTo(temp);
		msg.setType(Protocol.HEARTBEAT_RESPONSE_TYPE);
		writer.registerWriteChannel(msg.getChannel(), msg);
	}
	
	private boolean handleToken(CommonMsg msg){
		if(msg.getType() == Protocol.REGISTER_TYPE||msg.getType() == Protocol.LOGIN_TYPE)
			return true;
		ClientConnection connection = clients.get(msg.getFrom());
		if(connection!=null){
			String token = clients.get(msg.getFrom()).getToken();
			return msg.getToken().equals(token);
		}else{
			return false;
		}
		
	}
	
	private void handleFile(CommonMsg msg){//负责离线或把发送文件的请求告诉目标
		ClientConnection client = clients.get(msg.getTo()); 
		if(client == null){
			FileEntity entity = new Gson().fromJson(new String(msg.getData()), FileEntity.class);
			msg.setType(Protocol.FILE_RESPONSE_TYPE);
			ServerFileResponse response = new ServerFileResponse();
			response.setFile_id(helper.insertFileMsg(msg.getFrom(), msg.getTo(), entity.getFile_name(), entity.getFile_length(), entity.getFile_suffix()));
			response.setIs_off(true);
			response.setPort(ConnectionManager.FILE_PORT);
			response.setYes(true);
			msg.setData(new Gson().toJson(response).getBytes());
			writer.registerWriteChannel(clients.get(msg.getFrom()).getChannel(), msg);
		}else{		
			
			writer.registerWriteChannel(clients.get(msg.getTo()).getChannel(), msg);
		}
	}
	
	private void handleFileResponse(CommonMsg msg){//负责处理目标传回的回应，同意或拒绝
		ServerFileResponse response = new Gson().fromJson(msg.getData().toString(), ServerFileResponse.class);
		response.setIp(clients.get(msg.getFrom()).getIp());
		writer.registerWriteChannel(clients.get(msg.getFrom()).getChannel(), msg);
	}
	
	
	private void handleTransmitionResponse(CommonMsg msg){
		ServerFileResponse response = new Gson().fromJson(msg.getData().toString(), ServerFileResponse.class);
		String path = helper.getFilePath(msg.getMsg_id());
		if(response.isYes()){
			synchronized(MessageHandler.this){
				if(ConnectionManager.threads.get(msg.getTo())==null){
					List<String> files = new ArrayList<String>();
					List<Byte> ids = new ArrayList<Byte>();
					ids.add(response.getFile_id());
					files.add(path);
					Socket s = new Socket();
					try {
						s.connect(new InetSocketAddress(ConnectionManager.clients.get(msg.getTo()).getIp(),response.getPort()));
						FileSendThread thread = new FileSendThread(s, files, ids);
						ConnectionManager.threads.put(msg.getTo(), thread);
						new Thread(thread).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					ConnectionManager.threads.get(msg.getTo()).files.add(path);
					ConnectionManager.threads.get(msg.getTo()).msg_ids.add(response.getFile_id());
				}
				}
			}
		else{
			helper.deleteFile(msg.getMsg_id());
			File file = new File(path);
			file.delete();
			
		}
	}
	
	
	
	private void handleGroupMsg(CommonMsg msg){
		ArrayList<String> ids = helper.getGroupMember(msg.getTo());
		for(String id:ids){
			writer.registerWriteChannel(clients.get(id).getChannel(), msg);
		}
	}
	
	private void handleCreateGroup(CommonMsg msg){
		String group_id = helper.createGroup(msg.getData().toString(), msg.getTime(), msg.getFrom());
		msg.setData(group_id.getBytes());
		msg.setType(Protocol.GROUP_RESPONSE_TYPE);
		writer.registerWriteChannel(msg.getChannel(), msg);
	}
	
	private void handleJoinGroup(CommonMsg msg){
		helper.joinGroup(msg.getFrom(), msg.getData().toString(), msg.getTime());
	}
	
	private void handleFriend(CommonMsg msg){
		String friend_msg = new String(msg.getData());	
		ClientConnection friend = clients.get(msg.getTo());
		if(friend!=null){
			FriendMsg f_msg = helper.getFriendMsg(msg.getFrom());
			f_msg.setFriend_msg(friend_msg);
			msg.setData(new Gson().toJson(f_msg).getBytes());
			writer.registerWriteChannel(clients.get(msg.getTo()).getChannel(), msg);
		}else{
			System.out.println("insert friend");
			System.out.println(new String(msg.getData()));
			helper.insertMsg(Protocol.FRIEND_TYPE, msg.getFrom(), msg.getTo(),msg.getData());
		}
		
	}
	
	private void handleFriendResponse(CommonMsg msg){
		helper.addFriend(msg.getFrom(), msg.getTo());
		FriendMsg f_msg = helper.getFriendMsg(msg.getFrom());
		msg.setData(new Gson().toJson(f_msg).getBytes());
		writer.registerWriteChannel(clients.get(msg.getTo()).getChannel(), msg);
	}
	


	
	/*private void handleFileReceive(CommonMsg msg){
		try {
			FileMessage file_msg = new Gson().fromJson((msg.getData()).toString(), FileMessage.class);
			String name = file_msg.getFile_name();
			byte[] file_data = file_msg.getData();
			RandomAccessFile aFile = new RandomAccessFile("data/"+"nio-data.txt", "rw");
			FileChannel outChannel = aFile.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}*/
	
	public byte[] getData(byte[] data,int off){
		 byte[] newData = new byte[off-4];
		 for(int i = 0;i<off-4;i++){
			 newData[i] = data[i+4];
			 //System.out.println("newData:"+newData[i]);
		 }
		 return newData;
		 
		 
	 }
	
}
