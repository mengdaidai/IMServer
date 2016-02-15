package Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import DB.DBHelper;


public class ServerDriver {
	public static void main(String[] args){
		ConnectionManager server = new ConnectionManager();
		server.start();
		/*DatagramChannel dc;
		try {
			dc = DatagramChannel.open();
			dc.configureBlocking( false ) ; 
			SocketAddress address = new InetSocketAddress(12112 ) ;
		      //本地绑定端口
		      DatagramSocket ds = dc.socket() ;
		      ds.setReceiveBufferSize(20480);
		      ds.bind( address ) ;
		      Selector selector = Selector.open() ;
		      dc.register( selector , SelectionKey.OP_READ ) ;
		      System.out.println( "Listening on port "+12112 );
		     
		  ByteBuffer buffer = ByteBuffer.allocate(1024);
		  while(true){
			  selector.select();
			  Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
			  while(iter.hasNext()){
				  SelectionKey key = iter.next();
				  if(key.isReadable()){
					  DatagramChannel cc = ( DatagramChannel )key.channel() ;
					  cc.configureBlocking(false);
					  //buffer.clear();
			            SocketAddress client = cc.receive( buffer ) ;
			            byte[] b = buffer.array();
			            System.out.println(new String(b));
				  }
			  }
		  }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	      
		
		
	//	DBHelper helper = new DBHelper();
		//helper.register( "hehiehihei","111");
	    
	}
}
