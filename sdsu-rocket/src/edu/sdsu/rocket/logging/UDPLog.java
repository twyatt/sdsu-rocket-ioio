package edu.sdsu.rocket.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.sdsu.rocket.network.UDPClient;

public class UDPLog implements Logger {

	private final UDPClient client;
	
	public UDPLog(InetAddress inetAddress, int port) {
		client = new UDPClient(inetAddress, port);
	}
	
	public UDPLog(String host, int port) throws UnknownHostException {
		client = new UDPClient(host, port);
	}
	
	public void i(String tag, String msg) {
		client.send(tag + ": " + msg);
	}

	@Override
	public void e(String tag, String msg, Exception e) {
		// TODO Auto-generated method stub
		
	}
}
