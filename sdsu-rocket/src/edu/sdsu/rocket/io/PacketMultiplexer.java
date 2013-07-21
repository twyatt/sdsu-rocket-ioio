package edu.sdsu.rocket.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketMultiplexer implements PacketWriter {
	
	final List<PacketWriter> handlers = new ArrayList<PacketWriter>();
	
	int i = 0;
	
	public PacketMultiplexer(PacketWriter ... handlers) {
		for (PacketWriter handler : handlers) {
			add(handler);
		}
	}
	
	public void add(PacketWriter sb70) {
		this.handlers.add(sb70);
	}

	public void write(Packet packet) throws IOException {
		writePacket(packet.messageId, packet.data);
	}
	
	public void write(byte id, byte data) throws IOException {
		writePacket(id, new byte[] { data });
	}
	
	public void writePacket(byte id, byte[] data) throws IOException {
		for (PacketWriter handler : handlers) {
			handler.writePacket(id, data);
		}
	}

}
