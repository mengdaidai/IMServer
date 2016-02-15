package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import entity.FileEntity;
import entity.FriendMsg;
import entity.TransmitionMessage;
import Server.Protocol;
import Util.Util;

public class DBHelper {
	String url = "jdbc:mysql://localhost:3306/androidim";
	Connection conn = null;
	Statement stat = null;
	
	public DBHelper(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection)DriverManager.getConnection(url,"root","root");
			stat = (Statement)conn.createStatement();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean login(String user_id,String password){
		boolean isRight = false;
		String sqlString = "select * from user";	
		try {
			ResultSet rs =  stat.executeQuery(sqlString);
			while (rs.next()){                              
				String user = rs.getString(1);
				String passw = rs.getString(2);
				if(user_id.equals(user)&&password.equals(passw))	{
					isRight = true;
					break;
				}
				if(user_id.equals(user)&&!password.equals(passw)){
					break;
				}
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isRight;
	}
	
	
	public synchronized String register (String nickname,String password){
		String user_id = getNewID();
		System.out.println("从数据库得到ID:"+user_id);
		String sql1 = "INSERT USER VALUES('"+user_id+"','"+password+"')";
		String sql2 = "INSERT USERINFO(user_id,nick_name) VALUES('"+user_id+"','"+nickname+"')";
		try {
			System.out.println("数据库注册插入");
			stat.executeUpdate(sql1);
			stat.executeUpdate(sql2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user_id;
	}
	
	public String getNewID(){
		String sql = "select user_id from user where user_id = (select max(user_id) from user)";
		String id = null ;
		try {
			ResultSet rs = stat.executeQuery(sql);
			rs.next();
			id = rs.getString(1);
			int ID = Integer.parseInt(id);
			ID++;
			id = Integer.toString(ID);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public void insertMsg(byte type,String from,String to,byte[] data){
			try {
				String sql;
				switch(type){
				case Protocol.TEXT_TYPE:
					String text = new String(data);
					sql = "insert message(message_text,message_from,message_to) values('"+text+"','"+from+"','"+to+"')";
					stat.executeUpdate(sql);
					break;
				case Protocol.VOICE_TYPE:
					sql = "insert message(message_voice,message_from,message_to) values('"+data+"','"+from+"','"+to+"')";
					stat.executeUpdate(sql);
					break;
				case Protocol.PICTURE_TYPE:
					sql = "insert message(message_pic,message_from,message_to) values('"+data+"','"+from+"','"+to+"')";
					stat.executeUpdate(sql);
					break;
				case Protocol.FRIEND_TYPE:
					String text1 = new String(data);
					sql = "insert message(message_friend,message_from,message_to) values ('"+text1+"','"+from+"','"+to+"')";
					stat.executeUpdate(sql);
			}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
	}
	
	public synchronized byte insertFileMsg(String from,String to,String file_name,int file_length,String file_suffix){
		byte msg_id = 0;
		String sql = "insert message(message_from,message_to,message_file_name,message_file_length,message_file_suffix) "
				+ "values('"+from+"','"+to+"','"+file_name+"','"+file_length+"','"+file_suffix+"')";
		String sql1 = "select max(message_id) from message";
		try {
			stat.executeUpdate(sql);
			ResultSet rs = stat.executeQuery(sql1);
			rs.next();
			msg_id = (byte) rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return msg_id;
		
	}
	
	public ArrayList<TransmitionMessage> getOfflineMsg(String user_id){
		ArrayList<TransmitionMessage> list = new ArrayList<TransmitionMessage>();
		try{
			
			String sql = "select * from message where message_to = '"+user_id+"'";
			ResultSet rs = stat.executeQuery(sql);
			while(rs.next()){
				TransmitionMessage msg = new TransmitionMessage();
				msg.setTime(rs.getString(9));
				msg.setFrom(rs.getString(3));
				msg.setMsg_id((byte)rs.getInt(1));
				if(rs.getString(2)!=null){
					msg.setType(Protocol.TEXT_TYPE);
					msg.setData(rs.getString(2).getBytes());
				}else if(rs.getBlob(5)!=null){
					msg.setType(Protocol.PICTURE_TYPE);
					msg.setData(rs.getBlob(5).getBytes(0, (int)rs.getBlob(5).length()));
				}else if(rs.getString(6)!=null){
					msg.setType(Protocol.VOICE_TYPE);
					msg.setData(rs.getBlob(6).getBytes(0, (int)rs.getBlob(5).length()));
				}else if(rs.getString(7)!=null){
					msg.setType(Protocol.FILE_TYPE);
					FileEntity entity = new FileEntity();
					entity.setFile_length(rs.getInt(13));
					entity.setFile_name(rs.getString(10));
					entity.setFile_suffix(rs.getString(14));
					msg.setData(new Gson().toJson(entity).getBytes());
				}else if(rs.getString(8)!=null){
					msg.setType(Protocol.FRIEND_TYPE);
					FriendMsg f_msg = new FriendMsg();
					f_msg.setFriend_msg(rs.getString(8));			
					msg.setData(new Gson().toJson(f_msg).getBytes());
				}
				list.add(msg);
				
				
			}
			for(TransmitionMessage m:list){
				if(m.getType() == Protocol.FRIEND_TYPE){
					ResultSet rs1 = stat.executeQuery("select * from userinfo where user_id = '"+m.getFrom()+"'");
					rs1.next();
					FriendMsg fm = new Gson().fromJson(new String(m.getData()), FriendMsg.class);
					fm.setFriend_name(rs1.getString(2));
					fm.setFriend_sign(rs1.getString(4));
					//还没设置头像
					//f_msg.setHead_picture(rs1.getBlob(3).getBytes(0, (int) rs1.getBlob(3).length()));
					m.setData(new Gson().toJson(fm).getBytes());
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return list;
	}
	
	
	public ArrayList<String> getGroupMember(String group_id){
		ArrayList<String> ids = new ArrayList<String>();
		String sql = "select user_id from grouprelationship where group_id = '"+group_id+"'";
		ResultSet rs = null;
		try {
			rs = stat.executeQuery(sql);
			while(rs.next()){
				String id = rs.getString(1);
				ids.add(id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}
	
	public synchronized String createGroup(String name,byte[] time,String user_id){
		long time1 = Util.bytesToLong(time);
		String sql = "insert groups (group_name,create_time,member_num) values ('"+name+"','"+time+"',1)";
		String sql1 = "";
		String sql2 = "select max(group_id) from groups";
		String group_id = "";
		try {
			stat.executeUpdate(sql);
			ResultSet rs = stat.executeQuery(sql2);
			
			while(rs.next()){
				group_id = rs.getString(1);
				sql1 = "insert grouprelationship (group_id,user_id,join_time,identity) values ('"+group_id+"','"+user_id+"','"+time+"',0)";
			}
			stat.executeUpdate(sql1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return group_id;
		
	}
	
	public void joinGroup(String user_id,String group_id,byte[] time){
		long time1 = Util.bytesToLong(time);
		String sql = "insert grouprelationship(group_id,user_id,join_time,identity) values ('"+group_id+"','"+user_id+"','"+time1+"',2)";
		try {
			stat.executeUpdate(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<Object> getFileLength(int msg_id){
		String sql = "select message_file_length, message_file_suffix from message where message_id = '"+msg_id+"'";
		List<Object> file_info = new ArrayList<Object>();
		int length = 0;
		String suffix = "";
		try {
			ResultSet rs = stat.executeQuery(sql);
			rs.next();
			length = rs.getInt(1);
			suffix = rs.getString(2);
			file_info.add(length);
			file_info.add(suffix);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return file_info;
	}
	
	public String getFilePath(int msg_id){
		String sql = "select message_file from message where message_id = '"+msg_id+"'";
		String path = "";
		try {
			ResultSet rs = stat.executeQuery(sql);
			rs.next();
			path = rs.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	public void deleteFile(byte file_id){
		String sql = "delete from message where message_id = '"+file_id+"'";
		try {
			stat.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public FriendMsg getFriendMsg(String friend_id){
		FriendMsg msg = new FriendMsg();
		String sql = "select * from userinfo where user_id = '"+friend_id+"'";
		ResultSet rs;
		try {
			rs = stat.executeQuery(sql);
			rs.next();
			msg.setFriend_name(rs.getString(2));
			msg.setFriend_sign(rs.getString(4));
			msg.setHead_picture(rs.getBytes(3));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		return msg;
	}
	
	public void addFriend(String user1_id,String user2_id){
		String sql = "insert friends (user1,user2) values ('"+user1_id+"','"+user2_id+"')";
		try {
			stat.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
