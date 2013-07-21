package edu.sdsu.rocket.logging;

import java.io.IOException;

import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketWriter;

public class PacketLog implements Logger {

	private PacketWriter writer;

	public PacketLog(PacketWriter writer) {
		this.writer = writer;
	}
	
	public void log(Level level, String msg) {
		try {
			writer.writePacket(Packet.LOG_MESSAGE, msg.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Logger interface methods.
	 */
	
	@Override
	public void i(String tag, String msg) {
		log(Level.INFO, msg);
	}

	@Override
	public void e(String tag, String msg) {
		log(Level.ERROR, msg);
	}

	@Override
	public void e(String tag, String msg, Throwable e) {
		log(Level.ERROR, msg + "\n" + e.getMessage());
	}

}
