package edu.sdsu.rocket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
	
	public static final int UDP_PORT = 47050;
	public static final int TCP_PORT = 47250;

	static public void register(EndPoint endPoint) {
		Kryo kyro = endPoint.getKryo();
		
		kyro.register(LoggingRequest.class);
		kyro.register(LogMessage.class);
	}
	
	static public class LoggingRequest {
		public boolean enable;
	}
	
	static public class LogMessage {
		public int level;
		public String tag;
		public String message;
	}
	
}
