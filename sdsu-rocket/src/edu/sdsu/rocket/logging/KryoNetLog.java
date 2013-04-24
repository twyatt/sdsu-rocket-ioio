package edu.sdsu.rocket.logging;

import java.util.HashSet;
import java.util.Set;

import com.esotericsoftware.kryonet.Connection;

import edu.sdsu.rocket.Network.LogMessage;

public class KryoNetLog implements Logger {
	
	public static final int LEVEL_TRACE = 1;
	public static final int LEVEL_DEBUG = 2;
	public static final int LEVEL_INFO  = 3;
	public static final int LEVEL_WARN  = 4;
	public static final int LEVEL_ERROR = 5;
	public static final int LEVEL_NONE  = 6;
	
	private Set<Connection> connections = new HashSet<Connection>();

	public void addConnection(Connection connection) {
		if (connection != null) {
			connections.add(connection);
		}
	}
	
	public void removeConnection(Connection connection) {
		connections.remove(connection);
	}
	
	private void sendLog(LogMessage log) {
		for (Connection connection : connections) {
			connection.sendTCP(log);
		}
	}
	
	/*
	 * Logger interface methods.
	 */
	
	@Override
	public void i(String tag, String msg) {
		LogMessage log = new LogMessage();
		
		log.level = LEVEL_INFO;
		log.tag = tag;
		log.message = msg;
		
		sendLog(log);
	}


	@Override
	public void e(String tag, String msg, Exception e) {
		LogMessage log = new LogMessage();
		
		log.level = LEVEL_ERROR;
		log.tag = tag;
		log.message = msg + "\n\n" + e.getMessage();
		
		sendLog(log);
	}

}
