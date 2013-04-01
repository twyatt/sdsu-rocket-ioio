package edu.sdsu.rocket.control.logging;

import edu.sdsu.rocket.network.UDPClient;

public class UDPLog implements Logger {

	private final UDPClient client;
	
	public UDPLog(String host, int port) {
		client = new UDPClient(host, port);
	}
	
	public void i(String tag, String msg) {
		client.send(tag + ": " + msg);
	}
}
