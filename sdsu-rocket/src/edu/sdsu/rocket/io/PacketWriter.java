package edu.sdsu.rocket.io;

import java.io.IOException;

public interface PacketWriter {
	
	public void write(Packet packet) throws IOException;
	public void writePacket(byte id, byte[] data) throws IOException;

}
