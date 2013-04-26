package edu.sdsu.rocket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
	
	public static final int LOG_LEVEL_TRACE = 1;
	public static final int LOG_LEVEL_DEBUG = 2;
	public static final int LOG_LEVEL_INFO  = 3;
	public static final int LOG_LEVEL_WARN  = 4;
	public static final int LOG_LEVEL_ERROR = 5;
	public static final int LOG_LEVEL_NONE  = 6;
	
	public static final int UDP_PORT = 47050;
	public static final int TCP_PORT = 47250;

	static public void register(EndPoint endPoint) {
		Kryo kyro = endPoint.getKryo();
		
		kyro.register(AuthenticationRequest.class);
		kyro.register(LoggingRequest.class);
		kyro.register(LogMessage.class);
		kyro.register(LaunchRequest.class);
	}
	
	static public class AuthenticationRequest {
		public String key;
	}
	
	static public class LoggingRequest {
		public boolean enable;
	}
	
	static public class LogMessage {
		public int level;
		public String tag;
		public String message;
	}
	
	static public class LaunchRequest {}
	
}
