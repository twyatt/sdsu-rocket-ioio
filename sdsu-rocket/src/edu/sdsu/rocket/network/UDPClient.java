package edu.sdsu.rocket.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient {
	
	private InetAddress inetAddress;
	private int port;
	private DatagramSocket socket;
	
	public UDPClient(InetAddress inetAddress, int port) {
		this.inetAddress = inetAddress;
		this.port = port;
		
		try {
			socket = new DatagramSocket(null);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public UDPClient(String host, int port) throws UnknownHostException {
		this(InetAddress.getByName(host), port);
	}

	public void send(String text) {
		send(text.getBytes());
	}
	
	public void send(byte[] bytes) {
		if (socket != null && inetAddress != null && port > 0) {
			try {
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length, inetAddress, port);
				socket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
