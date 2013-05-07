package edu.sdsu.rocket.console;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.esotericsoftware.kryonet.Client;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.LoggingRequest;

public class RocketClient {

	public Client kryonet = new Client();
	
	private String manualHost;
	private int tcpPort;
	private int udpPort;
	
	private int discoverTimeout;
	private int connectTimeout;
	
	/**
	 * Thread loop sleep duration (milliseconds).
	 */
	private long sleep;
	
	public RocketClient(int threadSleep) {
		sleep = threadSleep;
		start();
	}
	
	/**
	 * Sets the discover host timeout.
	 * 
	 * @param discoverTimeout Discover timeout in seconds.
	 * @return
	 */
	public RocketClient setDiscoverTimeout(int discoverTimeout) {
		this.discoverTimeout = discoverTimeout;
		return this;
	}
	
	/**
	 * Sets the connect timeout.
	 * 
	 * @param connectTimeout Connect timeout in seconds.
	 * @return
	 */
	public RocketClient setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}
	
	public RocketClient setHost(String host) {
		this.manualHost = host;
		return this;
	}
	
	public RocketClient setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
		return this;
	}
	
	public RocketClient setUdpPort(int udpPort) {
		this.udpPort = udpPort;
		return this;
	}
	
	public void start() {
		Network.register(kryonet);
		kryonet.start();
	}
	
	public void stop() {
		kryonet.stop();
	}
	
	public boolean connect() throws UnknownHostException {
		InetAddress address;
		
		if (manualHost != null) {
			address = InetAddress.getByName(manualHost);
			
			try {
				System.out.println("Connecting to " + address + ".");
				kryonet.connect(connectTimeout, address, tcpPort, udpPort);
				if (kryonet.isConnected()) {
					return true;
				} else {
					System.err.println("Failed to connect to " + address + ".");
				}
			} catch (IOException e) {
				// ignore
			}
		}
		
		System.out.println("Discovering host.");
		address = kryonet.discoverHost(udpPort, discoverTimeout);
		if (address == null) {
			System.err.println("Failed to discover host.");
			return false;
		}
		
		try {
			System.out.println("Connecting to " + address + ".");
			kryonet.connect(connectTimeout, address, tcpPort, udpPort);
		} catch (IOException e) {
			System.err.println("Failed to connect to " + address + ".");
		}
	
		return kryonet.isConnected();
	}

	public void enableRemoteLogging() {
		LoggingRequest loggingRequest = new LoggingRequest();
		loggingRequest.enable = true;
		kryonet.sendTCP(loggingRequest);
	}
	
	/**
	 * Blocks until connection is lost.
	 */
	public void loop() {
		while (kryonet.isConnected()) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
}
