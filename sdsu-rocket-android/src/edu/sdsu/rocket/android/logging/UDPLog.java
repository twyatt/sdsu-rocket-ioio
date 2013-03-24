package edu.sdsu.rocket.android.logging;

import edu.sdsu.aerospace.rocket.network.UDPClient;

public class UDPLog implements Logger {

	private final UDPClient client;
	
	public UDPLog(String host, int port) {
		client = new UDPClient(host, port);
	}
	
	public void i(String tag, String msg) {
		client.send(tag + ": " + msg);
	}
}
