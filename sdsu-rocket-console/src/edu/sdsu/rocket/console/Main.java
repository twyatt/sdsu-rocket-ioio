package edu.sdsu.rocket.console;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.AuthenticationRequest;
import edu.sdsu.rocket.Network.CommandRequest;
import edu.sdsu.rocket.Network.LogMessage;
import edu.sdsu.rocket.Network.LoggingRequest;
import edu.sdsu.rocket.Network.SetObjectiveRequest;

public class Main {
	
	private static final int CONNECT_TIMEOUT = 10000; // milliseconds
	private static final int DISCOVER_TIMEOUT = 5000; // milliseconds

	private static Client client = new Client();
	
	// TODO change to array
	private static Runnable runnable;

	public static void main(String[] argv) {
		// http://code.google.com/p/kryonet/issues/detail?id=29
//		System.setProperty("java.net.preferIPv4Stack" , "true");
		
		setup();
		if (!connect()) {
			if (!connect("192.168.1.3")) {
				return;
			}
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		
		LoggingRequest loggingRequest = new LoggingRequest();
		loggingRequest.enable = true;
		client.sendTCP(loggingRequest);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		
		System.err.println("Sending authentication key.");
		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.key = Network.AUTHENTICATION_KEY;
		client.sendTCP(authenticationRequest);
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			return;
//		}
//		
//		System.err.println("Sending set objective request.");
//		SetObjectiveRequest setObjectiveRequest = new SetObjectiveRequest();
//		setObjectiveRequest.name = "launch";
//		client.sendTCP(setObjectiveRequest);
//		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			return;
//		}
//		
//		System.err.println("Sending launch request.");
//		CommandRequest commandRequest = new CommandRequest();
//		commandRequest.command = "launch";
//		client.sendTCP(commandRequest);
		
		loop();
	}
	
	private static boolean connect() {
		System.out.println("Searching for host.");
		
		int udpPort = Network.UDP_PORT;
		InetAddress address = client.discoverHost(udpPort, DISCOVER_TIMEOUT);
		
		if (address != null) {
			return connect(address);
		} else {
			System.err.println("Failed to discover host.");
			return false;
		}
	}
	
	private static boolean connect(String host) {
		try {
			InetAddress inetAddress = InetAddress.getByName(host);
			return connect(inetAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean connect(InetAddress address) {
		System.out.println("Connecting to " + address + ".");
		
		try {
			client.connect(CONNECT_TIMEOUT, address, Network.TCP_PORT, Network.UDP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private static void loop() {
		while (true) {
			try {
				if (runnable != null) {
					runnable.run();
					runnable = null;
				}
				
				Thread.sleep(500);
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
					
					if (log.level >= Network.LOG_LEVEL_ERROR) {
						System.err.println("ERROR: " + log.message);
					} else {
						System.out.println("LOG: " + log.message);
					}
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.out.println("Disconnected.");
				
				postRunnable(new Runnable() {
					@Override
					public void run() {
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
		});
	}

	public static void postRunnable(Runnable runnable) {
		Main.runnable = runnable;
	}
	
}
