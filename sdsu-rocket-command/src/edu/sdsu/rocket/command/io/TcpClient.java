package edu.sdsu.rocket.command.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import edu.sdsu.rocket.helpers.Threaded;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketInputStream;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketOutputStream;
import edu.sdsu.rocket.io.PacketWriter;

public class TcpClient extends Threaded implements PacketWriter {
	
	final private static int MAX_DATA_LENGTH = 1024000; // bytes
	
	public interface TcpClientListener {
		public void onConnected();
		public void onDisconnected();
	}
	
	private TcpClientListener listener;
	private PacketListener packetListener;
	
	private Socket socket;
	
	private PacketOutputStream out;
	private PacketInputStream in;
	
	public TcpClient() {
		setSleep(0L);
	}
	
	public TcpClient setListener(TcpClientListener listener) {
		this.listener = listener;
		return this;
	}
	
	public TcpClient setPacketListener(PacketListener listener) {
		if (listener == null)
			throw new NullPointerException();
		this.packetListener = listener;
		
		return this;
	}
	
	public void connect(InetAddress address, int port) throws IOException {
		socket = new Socket(address, port);
		
		in = new PacketInputStream(socket.getInputStream(), Packet.START_BYTES, MAX_DATA_LENGTH);
		out = new PacketOutputStream(socket.getOutputStream(), Packet.START_BYTES);
		
		start();
		
		if (listener != null)
			listener.onConnected();
	}
	
	public void disconnect() {
		
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stop();
		
		if (listener != null)
			listener.onDisconnected();
	}

	@Override
	public void write(Packet packet) {
		writePacket(packet.messageId, packet.data);
	}

	@Override
	public void writePacket(byte id, byte[] data) {
		try {
			out.writePacket(id, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Threaded interface methods.
	 */

	@Override
	public void loop() {
		try {
			packetListener.onPacketReceived(in.readPacket());
		} catch (IOException e) {
			packetListener.onPacketError(e);
		}
	}

	@Override
	public void interrupted() {
		// silently ignore
	}

}
