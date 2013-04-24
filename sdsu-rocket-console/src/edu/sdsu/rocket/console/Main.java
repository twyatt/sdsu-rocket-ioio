package edu.sdsu.rocket.console;

import java.io.IOException;
import java.net.InetAddress;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.LogMessage;
import edu.sdsu.rocket.Network.LoggingRequest;
import edu.sdsu.rocket.logging.KryoNetLog;

public class Main {
	
	private static Client client = new Client();

	public static void main (String[] argv) {
		setup();
		connect();
		
		LoggingRequest loggingRequest = new LoggingRequest();
		loggingRequest.enable = true;
		client.sendTCP(loggingRequest);
		
		loop();
	}

	private static void loop() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	private static void setup() {
		Network.register(client);
		client.start();
		
		client.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				System.out.println("Connected to " + connection.getRemoteAddressTCP() + ".");
			}
			
			@Override
			public void received(Connection connection, Object object) {
//				System.out.println("received from " + connection.getID());
				
				if (object instanceof LogMessage) {
					LogMessage log = (LogMessage)object;
					
					if (log.level >= KryoNetLog.LEVEL_ERROR) {
						System.err.println("ERROR: " + log.message);
					} else {
						System.out.println("LOG: " + log.message);
					}
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.out.println("Disconnected from " + connection.getRemoteAddressTCP());
				
				int timeout = 30000; // milliseconds
				try {
					System.out.println("Reconnecting ...");
					client.reconnect(timeout);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		});
	}
	
	private static void connect() {
		int udpPort = Network.UDP_PORT;
		int timeout = 30000; // milliseconds
		InetAddress address = client.discoverHost(udpPort, timeout);
		
		System.out.println("address = " + address);
		
		if (address == null) {
			try {
				System.out.println("Connecting ...");
				client.connect(timeout, "192.168.1.108", Network.TCP_PORT, Network.UDP_PORT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
}
