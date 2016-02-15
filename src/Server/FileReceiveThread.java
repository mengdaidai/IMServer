package Server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.List;

import DB.DBHelper;
import Util.Util;

public class FileReceiveThread implements Runnable{
	
	Socket s ;
	InputStream is;
	DataInputStream dis;
	DBHelper helper;
	public FileReceiveThread(Socket s){
		this.s = s;
		try {
			is = s.getInputStream();
			dis = new DataInputStream(is);
			helper = new DBHelper();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void run() {
		try {
		int byte_read = 0;
		byte[] buffer = new byte[5*1024];
		byte[] data = new byte[4];
		int off = 0;
		int data_off =0;
		int file_length = 0;
		int file_id;
		int start_pos = 0;
		boolean startOfFile = true;
		File file_dir = new File("G:/IMServer_File");
		if(!file_dir.exists()){
			file_dir.mkdirs();
		}
		File file;
		FileOutputStream fos = null;
		//RandomAccessFile raf = new RandomAccessFile(file,"rw");
		int i =0;
		while(true){
				if((byte_read = dis.read(buffer))!=-1){
					i++;
					System.out.println("得到一些数据"+byte_read+"      "+i);
					//raf.seek(off);
					off+=byte_read;
					System.out.println("off:"+off/1024);
					if(off>file_length&&file_length!=0){
						System.out.println("开始接受第二个文件");
						fos.write(buffer,0,byte_read-off+file_length);
						fos.flush();
						off = off-file_length;
						file_length = 0;
						data_off = 0;
						System.out.println(off/1024);
						startOfFile = true;
						start_pos = byte_read-off;
					}
					else if(off == file_length){
						System.out.println("开始接受第二个文件1");
						fos.write(buffer,0,byte_read);
						fos.flush();
						file_length = 0;
						data_off = 0;
						off = 0;
						startOfFile = true;
						start_pos = 0;
						continue;
					} 
					if(off>=4&&startOfFile){
						System.arraycopy(buffer, start_pos, data, data_off, 4-data_off);
						file_id = Util.getLength(data);
						System.out.println("file_id: "+file_id);
						List<Object> list = helper.getFileLength(file_id);
						file_length = (int) list.get(0);
						file_length+=4;
						System.out.println("length:"+file_length/1024);
						startOfFile = false;
						file = File.createTempFile("file","."+(String)list.get(1),file_dir);
						fos = new FileOutputStream(file);
						fos.write(buffer,4-data_off,byte_read-4+data_off);
					}else if(!startOfFile){
						fos.write(buffer,0,byte_read);
						System.out.println("写入文件  "+byte_read);
					}else if(off<4){
						System.arraycopy(buffer, start_pos, data, data_off, byte_read);
						data_off+=byte_read;
					}	
				}
		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
