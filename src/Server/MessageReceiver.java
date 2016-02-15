package Server;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import Util.Util;
import entity.CommonMsg;


public class MessageReceiver implements Runnable{
	public static final int BUFFER_SIZE = 1024;//缓冲区大小须再议
	Selector read_selector;
	MessageHandler handler;
	//boolean isBlocked =true;
	public MessageReceiver(){
		try{
			handler = new MessageHandler();
			read_selector = Selector.open();
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	@Override
	public void run() {
		try{
			readMessage();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	public void registerReadChannel(SocketChannel channel,CommonMsg msg){
		try{
			channel.configureBlocking(false);
			System.out.println("before wakeup selector");
				read_selector.wakeup();//因为主线程register和子线程select同时访问SelectionKey，所以可能会阻塞
				System.out.println("after wakeup selector");
			channel.register(read_selector, SelectionKey.OP_CONNECT |SelectionKey.OP_READ,msg);
			System.out.println("after register the channel");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void readMessage() throws IOException{
		System.out.println("readMessage!");
		while(true){

			
			//isBlocked = true;
			//System.out.println("before select!");
			int num = read_selector.select(500);
			//isBlocked = false;
			//System.out.println("num:"+num+isBlocked);
			//System.out.println("after select");
			if(num == 0)
				continue;
			Iterator<SelectionKey> iter = read_selector.selectedKeys().iterator();
			while(iter.hasNext()){
				System.out.println("hasNext:"+iter);
				SelectionKey key = iter.next();
				iter.remove();
				SocketChannel channel = (SocketChannel) key.channel();
				if(key.isReadable()){
					//System.out.println("readable");
					ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
					int byteRead = channel.read(buffer);
					CommonMsg msg = (CommonMsg) key.attachment();
					if(msg == null){
						msg = new CommonMsg();
					}
					byte[] data = msg.getData();
					int off = msg.getOff();
					boolean getLength = false;
					while(byteRead > 0){
						System.out.println("byteRead:"+byteRead);
						
						//System.out.println("key:"+key);
						//System.out.println("data.length:"+data.length);
						///System.out.println("off+byteRead:"+(off+byteRead));
						if(off+byteRead>data.length){
							//System.out.println("扩容");
							data = Util.grow(data,BUFFER_SIZE*2);
							//System.out.println("扩容后："+data.length);
						}
						System.arraycopy(buffer.array(), 0, data, off, byteRead);
						off+=byteRead;
						System.out.println("off:"+off);
						if(!getLength){
							if(off>=Protocol.LENGTH_BYTE){
								System.out.println("设置长度");
								getLength = true;
								msg.setLength(Util.getLength(data));
							}
						}
						/*if(!getType){
							if(off>=Protocol.TYPE_START+Protocol.TYPE_BYTE){
								msg.setType(data[Protocol.LENGTH_BYTE]);
								if(msg.getType() == Protocol.FILE_TRANS_TYPE){
									buffer = Util.reallocateBuffer(buffer, 5000);
								}			
						}	
						}*/
						buffer.clear();
						//System.out.println("temp:"+new String(getData(data,off)));
						//System.out.println("temp:"+new String(Arrays.copyOfRange(data, Protocol.HEAD_LENGTH, off)));
						byteRead = channel.read(buffer);
					}
					System.out.println("出循环！");
					msg.setOff(off);
					msg.setData(data);
					msg.setChannel(channel);
					System.out.println(msg.getOff());
					System.out.println(msg.getLength());
					if(msg.getOff() == msg.getLength()){
						System.out.println("开始处理消息！");
						handler.handleMsg(msg);
					}
					
					
				}
			}
			
		}
	}
	

	
	
	
	
	
	
	 
	 

	 public byte[] getData(byte[] data,int off){
		 byte[] newData = new byte[off-5];
		 for(int i = 0;i<off-5;i++){
			 newData[i] = data[i+5];
		 }
		 return newData;
		 
		 
	 }
}
