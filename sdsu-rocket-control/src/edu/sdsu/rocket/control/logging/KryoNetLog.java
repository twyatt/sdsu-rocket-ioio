package edu.sdsu.rocket.control.logging;

import java.util.HashSet;
import java.util.Set;

import com.esotericsoftware.kryonet.Connection;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.LogMessage;
import edu.sdsu.rocket.logging.Logger;

public class KryoNetLog implements Logger {
	
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
		
		log.level = Network.LOG_LEVEL_INFO;
		log.tag = tag;
		log.message = msg;
		
		sendLog(log);
	}


	@Override
	public void e(String tag, String msg, Exception e) {
		LogMessage log = new LogMessage();
		
		log.level = Network.LOG_LEVEL_ERROR;
		log.tag = tag;
		log.message = msg + "\n\n" + e.getMessage();
		
		sendLog(log);
	}

}
