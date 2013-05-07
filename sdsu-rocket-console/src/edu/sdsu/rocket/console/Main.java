package edu.sdsu.rocket.console;

import java.net.UnknownHostException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.AuthenticationRequest;
import edu.sdsu.rocket.Network.LogMessage;
import edu.sdsu.rocket.Network.LoggingRequest;

public class Main {
	
	private static final int CONNECT_TIMEOUT = 5000; // milliseconds
	private static final int DISCOVER_TIMEOUT = 3000; // milliseconds

	public static void main(String[] argv) {
		// http://code.google.com/p/kryonet/issues/detail?id=29
//		System.setProperty("java.net.preferIPv4Stack" , "true");
		
		String host = (argv.length > 0 ? argv[0] : null);
		
		RocketClient client = new RocketClient(200 /* thread sleep (ms) */)
			.setDiscoverTimeout(DISCOVER_TIMEOUT)
			.setConnectTimeout(CONNECT_TIMEOUT)
			.setHost(host)
			.setUdpPort(Network.UDP_PORT)
			.setTcpPort(Network.TCP_PORT);
		
		client.kryonet.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				System.out.println("Connected to " + connection.getRemoteAddressTCP() + ".");
				
				System.out.println("Sending request to enable remote logging.");
				enableRemoteLogging(connection);
				
				System.out.println("Sending authentication key.");
				authenticate(connection);
			}
			
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof LogMessage) {
					onLogMessage((LogMessage)object);
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.out.println("Disconnected.");
			}
		});
		
		while (true) {
			try {
				client.connect();
			} catch (UnknownHostException e) {
				break;
			}
			client.loop();
		}
	}

	protected static void onLogMessage(LogMessage log) {
		if (log.level >= Network.LOG_LEVEL_ERROR) {
			System.err.println("ERROR: " + log.message);
		} else {
			System.out.println("LOG: " + log.message);
		}
	}

	protected static void authenticate(Connection connection) {
		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.key = Network.AUTHENTICATION_KEY;
		connection.sendTCP(authenticationRequest);
	}

	protected static void enableRemoteLogging(Connection connection) {
		LoggingRequest loggingRequest = new LoggingRequest();
		loggingRequest.enable = true;
		connection.sendTCP(loggingRequest);
	}
	
}
