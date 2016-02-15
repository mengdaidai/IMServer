package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import Util.Util;

public class FileSendThread implements Runnable{
	Socket s;
	OutputStream os;
	List<String> files;
	List<Byte> msg_ids;
	
	public FileSendThread(Socket s,List<String> files,List<Byte> msg_ids){
		this.s = s;
		this.files = files;
		this.msg_ids = msg_ids;
		try {
			os = s.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
	}

	@Override
	public void run() {
		byte[] buffer = new byte[5*1024];
		for(int i = 0;i<files.size();i++){
			String f = files.get(i);
			int msg_id = msg_ids.get(i);
			FileInputStream fis;
			try {
				fis = new FileInputStream(f);
				int byte_read = 0;
				byte[] id = Util.intToBytes(msg_id);
				os.write(id);
				while((byte_read = fis.read(buffer))!=-1){
					os.write(buffer,0,byte_read);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		try {
			os.flush();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}


}
