package Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import entity.CommonMsg;

public class MessageWriter implements Runnable{
	public static final int BUFFER_SIZE = 1024;//缓冲区大小须再议
	Selector write_selector;
	
	public MessageWriter(){
		try{
			write_selector = Selector.open();
			}catch(Exception e){
				e.printStackTrace();
			}
	}

	@Override
	public void run() {
		writeMsg();
		
	}
	
	public void registerWriteChannel(SocketChannel channel,CommonMsg msg){
		try {
			channel.configureBlocking(false);
			//write_selector.wakeup();//因为主线程register和子线程select同时访问SelectionKey，所以可能会阻塞
			channel.register(write_selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ |SelectionKey.OP_WRITE,msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeMsg(){
		while(true){
			try {
				int num = write_selector.select(500);
				//System.out.println("select:"+num);
				Iterator<SelectionKey> iter = write_selector.selectedKeys().iterator();
				while(iter.hasNext()){
					System.out.println("迭代器！");
					SelectionKey key = iter.next();
					iter.remove();
					SocketChannel channel = (SocketChannel)key.channel();
					CommonMsg msg = (CommonMsg)key.attachment();
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					if(key.isWritable()){
						System.out.println("写准备！");
						buffer.put(intToBytes(msg.getData().length+Protocol.HEAD_LENGTH));
						buffer.put(msg.getType());
						buffer.put(msg.getMsg_id());
						buffer.put(msg.getFrom().getBytes());
						buffer.put(msg.getToken().getBytes());
						buffer.put(msg.getTo().getBytes());
						buffer.put(msg.getTime());
						buffer.put(msg.getData());
						buffer.flip();
						while(buffer.hasRemaining()){
							int writeLength = channel.write(buffer);
							System.out.println("writeLength:"+writeLength);
						}
						key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);  
						/*if(msg.getType() == Protocol.REGISTER_RESPONSE_TYPE){
							System.out.println("断开连接！");
							key.channel().close();
							key.cancel();
						}*/
						
					}
					
					
					
					
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static int bytesToInt(byte[] bytes) {
	    int number = bytes[3] & 0xFF;  
	    //System.out.println(number);
	   // System.out.println(bytes[0]);
	    // "|="按位或赋值。  
	    number |= ((bytes[2] << 8) & 0xFF00); 
	    //System.out.println(number);
	    //System.out.println(bytes[1]);
	    number |= ((bytes[1] << 16) & 0xFF0000); 
	   // System.out.println(number);
	   // System.out.println(bytes[2]);
	    number |= ((bytes[0] << 24) & 0xFF000000);  
	   // System.out.println(number);
	    //System.out.println(bytes[3]);
	    return number;  
	} 
	
	public static byte[] intToBytes(int data){
		//System.out.println("data:"+data);
		byte[] b = new byte[4];
		
		b[3] = (byte)((data<<24)>>24);
		//System.out.println("intToByte:"+b[0]);
		b[2] = (byte)((data <<16)>> 24);
		//System.out.println("intToByte:"+b[1]);
		b[1] = (byte)((data << 8)>>24);
		//System.out.println("intToByte:"+b[2]);
		b[0] = (byte)(data >> 24);
		//System.out.println("intToByte:"+b[3]);
		return b;
	}
	
}
