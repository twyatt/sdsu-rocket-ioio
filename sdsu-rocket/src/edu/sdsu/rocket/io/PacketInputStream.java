package edu.sdsu.rocket.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Reads packets from an InputStream of the format:
 * 
 *   START_BYTES (byte[]) | MESSAGE_ID (byte) | LENGTH (int) | DATA (byte[])
 */
public class PacketInputStream extends DataInputStream {
	
	private final byte[] startBytes;
	private final int maxDataLength;
	
	public PacketInputStream(InputStream in, byte[] startBytes, int maxDataLength) {
		super(in);
		
		if (startBytes == null || startBytes.length == 0)
			throw new IllegalArgumentException();
		
		this.startBytes = startBytes;
		this.maxDataLength = maxDataLength;
	}

	public Packet readPacket() throws IOException {
		int position = 0;
		while (position < startBytes.length) {
			int sb = in.read();
			if (sb < 0)
				throw new EOFException();
			
			if ((byte) sb == startBytes[position]) {
				position++;
			} else {
				position = 0;
			}
		}
		
		int id = in.read();
		if (id < 0)
			throw new EOFException();
		
		int length = readInt();
		if (length > maxDataLength)
			throw new PacketException("Data length of " + length + " exceeds max of " + maxDataLength);
		
		byte[] data = null;
		if (length < 0) {
			// TODO drop packet
		} else if (length > 0) {
			if (length > maxDataLength) // TODO drop packet instead
				length = maxDataLength;
			data = new byte[length];
			readFully(data);
		}
		
		Packet packet = new Packet();
		packet.messageId = (byte) id;
		packet.data = data;
		
		return packet;
	}
	
	public class PacketException extends IOException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6032252841958123951L;
		
		public PacketException(String detailMessage) {
			super(detailMessage);
		}
		
	}
	
}
