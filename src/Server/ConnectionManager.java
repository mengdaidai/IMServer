package Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import entity.ClientConnection;
import entity.CommonMsg;
public class ConnectionManager {
	MessageReceiver tcp_receiver;
	//MessageWriter writer;
	ServerSocketChannel listen_channel;
	private static final int PORT = 8989;
	public static final int FILE_PORT = 9111;
	public static HashMap<String,ClientConnection> clients;
	public static ConcurrentHashMap<String,FileSendThread> threads;
	
	public ConnectionManager(){
		tcp_receiver = new MessageReceiver();
		//writer = new MessageWriter();
		clients = new HashMap<String,ClientConnection>();
		threads = new ConcurrentHashMap<String,FileSendThread>();
	}
	
	public void start(){
		try{
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress(FILE_PORT));
		new Thread(new FileServer(ss)).start();
		listen_channel = ServerSocketChannel.open();
		listen_channel.socket().bind(new InetSocketAddress(PORT));
		new Thread(tcp_receiver).start();
		//new Thread(writer).start();
		while(true){
			SocketChannel socket_channel = listen_channel.accept();
			System.out.println(socket_channel.getRemoteAddress());
			System.out.println("有新链接！");
			CommonMsg msg = null;
			tcp_receiver.registerReadChannel(socket_channel, msg);
			
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	class FileServer implements Runnable{
		ServerSocket ss = null;
		public FileServer(ServerSocket ss){
			this.ss = ss;
		}

		@Override
		public void run() {
			while(true){
				try {
					System.out.println("开始！");
					Socket s = ss.accept();
					System.out.println("有人进来啦！");
					new Thread(new FileReceiveThread(s)).start();;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
