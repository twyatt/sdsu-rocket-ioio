package edu.sdsu.rocket.control.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import edu.sdsu.rocket.logging.Log;


public class UDPServer {
	
	public interface UDPServerListener {
		public void onReceivedPacket(byte[] data, InetAddress inetAddress, int port);
	}
	

	private DatagramSocket socket;
	private volatile InterruptibleUDPThread interruptibleUDPThread;
	
	private UDPServerListener listener;
	
	public void setListener(UDPServerListener listener) {
		this.listener = listener;
	}
	
	public void listen(final int port) {
		stop();
		
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			Log.e("Failed to initialize UDP socket on port " + port + ".", e);
			return;
		}
		
		if (interruptibleUDPThread == null) {
			interruptibleUDPThread = new InterruptibleUDPThread();
			interruptibleUDPThread.start();
		}
		
		Log.i("Listening on port " + port + ".");
	}
	
	public void stop() {
		if (interruptibleUDPThread != null) {
			interruptibleUDPThread.interrupt();
			interruptibleUDPThread = null;
		}
	}
	
//	public void onReceivedPacket(byte[] data, InetAddress inetAddress, int port) {
//		String string = new String(data);
//		Log.i("onReceivedPacket: " + string);
//	}
	
	/**
	 * Interruptible network IO thread implementation.
	 * 
	 * {@link http://stackoverflow.com/questions/4670664/interrupt-a-thread-in-datagramsocket-receive}
	 */
	private class InterruptibleUDPThread extends Thread {
		
		@Override
		public void run() {
			super.run();
			
			byte[] buffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			
			/**
			 * Alternative to thread.stop() implementation.
			 * 
			 * {@link http://download.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html}
			 */
			Thread thisThread = Thread.currentThread();
			while (interruptibleUDPThread == thisThread) {
				try {
					packet.setLength(256);
					socket.receive(packet);
				} catch (SocketException se) {
					// http://www.cs.duke.edu/csed/java/jdk1.5/docs/api/java/net/DatagramSocket.html#close%28%29
					Log.e("Ignoring SocketException thrown from receive(), probably due to the socket being closed.", se);
					return;
				} catch (IOException ioe) {
					Log.e("IO Exception thrown in UDP thread.", ioe);
				}
				
				try {
					InetAddress inetAddress = packet.getAddress();
					int port = packet.getPort();
					
					//byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
					
					/**
					 * Using System.arraycopy instead of Arrays.copyOf as
					 * Arrays.copyOf is since Android API level 9.
					 * http://developer.android.com/reference/java/util/Arrays.html#copyOf%28byte[],%20int%29
					 * 
					 * "Just use System.arraycopy(), it's the most efficient way
					 * to copy arrays." -- Romain Guy
					 * https://groups.google.com/forum/?fromgroups#!topic/android-developers/4IFUyQkMZQw
					 */
					int length = packet.getLength();
					byte[] copyOf = new byte[length];
					System.arraycopy(buffer, 0, copyOf, 0, length);
					
					if (listener != null) {
						listener.onReceivedPacket(copyOf, inetAddress, port);
					}
				} catch (Exception e) {
					Log.e("Unknown exception, possibly due to peer leaving after sending packet?", e);
				}
			}
		}
		
		@Override
		public void interrupt() {
			super.interrupt();
			
			Log.i("Socket interrupted, closing.");
			socket.close();
		}
	}
	
}
