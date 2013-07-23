package edu.sdsu.rocket.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes packets to an OutputStream in the format:
 * 
 *   START_BYTES (byte[]) | MESSAGE_ID (byte) | LENGTH (int) | DATA (byte[])
 */
public class PacketOutputStream extends DataOutputStream implements PacketWriter {

	private final byte[] startBytes;

	public PacketOutputStream(OutputStream out, byte[] startBytes) {
		super(out);
		
		if (startBytes == null || startBytes.length == 0)
			throw new IllegalArgumentException();
		
		this.startBytes = startBytes;
	}
	
	/*
	 * PacketWriter interface methods.
	 */
	
	@Override
	public void write(Packet packet) throws IOException {
		writePacket(packet.messageId, packet.data);
	}
	
	@Override
	public void writePacket(byte messageId, byte[] data) throws IOException {
		write(startBytes);
		writeByte(messageId);
		if (data == null || data.length == 0) {
			writeInt(0);
		} else {
			writeInt(data.length);
			write(data);
		}
	}

}
