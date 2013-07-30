package edu.sdsu.rocket.command.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketInputStream;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketOutputStream;

public class TcpClient {
	
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
	
	private Thread readThread;
	private boolean isRunning;
	
	public TcpClient setListener(TcpClientListener listener) {
		this.listener = listener;
		return this;
	}
	
	public TcpClient setPacketListener(PacketListener listener) {
		this.packetListener = listener;
		return this;
	}
	
	public PacketOutputStream getOutputStream() {
		return out;
	}
	
	public void connect(InetAddress address, int port) throws IOException {
		socket = new Socket(address, port);
		
		in = new PacketInputStream(socket.getInputStream(), Packet.START_BYTES, MAX_DATA_LENGTH);
		out = new PacketOutputStream(socket.getOutputStream(), Packet.START_BYTES);
		
		start();
		
		if (listener != null) {
			listener.onConnected();
		}
	}
	
	public void disconnect() {
		stop();
		
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (listener != null) {
			listener.onDisconnected();
		}
	}
	
	private void start() {
		if (isRunning)
			throw new IllegalStateException("TCP read thread already running.");
		
		readThread = new Thread(new TcpReader());
		readThread.setName("TCP Read");
		readThread.start();
		isRunning = true;
	}
	
	private void stop() {
		if (isRunning) {
			readThread.interrupt();
		}
	}

	public class TcpReader implements Runnable {
		
		@Override
		public void run() {
			// http://stackoverflow.com/questions/141560/should-try-catch-go-inside-or-outside-a-loop
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Packet packet = in.readPacket();
					packetListener.onPacketReceived(packet);
				}
			} catch (IOException e) {
				e.printStackTrace();
				isRunning = false;
				disconnect();
			}
		}
		
	}

}
