package edu.sdsu.rocket.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class DatagramOutputStream extends OutputStream {
	
	private DatagramSocket socket;
	private InetSocketAddress address;
	
	private byte[] buffer = new byte[1];
	
	public DatagramOutputStream(InetSocketAddress address) throws SocketException {
		this.address = address;
		socket = new DatagramSocket();
	}

	@Override
	public void write(int oneByte) throws IOException {
		buffer[0] = (byte) oneByte;
		write(buffer, 0, 1);
	}
	
	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		if (data == null) {
			throw new NullPointerException();
		} else if ((offset < 0) || (offset > data.length) || (length < 0) ||
				((offset + length) > data.length) || ((offset + length) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (length == 0) {
			return;
		}
		
		DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
		socket.send(packet);
	}

}
