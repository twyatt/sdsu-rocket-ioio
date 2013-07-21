package edu.sdsu.rocket.io;

import java.io.IOException;

import edu.sdsu.rocket.helpers.Threaded;

/**
 * Manages a separate thread for receiving packets on.
 */
public class PacketInputThreader extends Threaded {

	private final PacketInputStream in;
	private final PacketListener listener;

	public PacketInputThreader(PacketInputStream in, PacketListener listener) {
		if (in == null)
			throw new NullPointerException();
		if (listener == null)
			throw new NullPointerException();
		
		this.in = in;
		this.listener = listener;
		
		setSleep(0L);
	}
	
	/*
	 * LooperRunnableListener interface methods.
	 */

	@Override
	public void loop() {
		try {
			listener.onPacketReceived(in.readPacket());
		} catch (IOException e) {
			listener.onPacketError(e);
		}
	}

	@Override
	public void interrupted() {
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
