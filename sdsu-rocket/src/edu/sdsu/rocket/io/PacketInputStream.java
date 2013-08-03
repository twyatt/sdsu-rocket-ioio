package edu.sdsu.rocket.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * Reads packets from an InputStream of the format:
 * 
 *   START_BYTES (byte[]) | MESSAGE_ID (byte) | LENGTH (int) | DATA (byte[]) | CRC32 (long)
 */
public class PacketInputStream extends DataInputStream {
	
	private static final boolean DEBUG = false;
	
	private final byte[] startBytes;
	private final int maxDataLength;
	private int position;
	
	public PacketInputStream(InputStream in, byte[] startBytes, int maxDataLength) {
		super(new CheckedInputStream(in, new CRC32()));
		
		if (startBytes == null || startBytes.length == 0) {
			throw new IllegalArgumentException();
		}
		
		this.startBytes = startBytes;
		this.maxDataLength = maxDataLength;
	}

	public Packet readPacket() throws IOException {
		((CheckedInputStream) in).getChecksum().reset();
		position = 0;
		
		if (DEBUG)
			System.out.println("START_BYTES");
			
		while (position < startBytes.length) {
			int sb = in.read();
			if (sb < 0) {
				throw new EOFException();
			}
			
			if ((byte) sb == startBytes[position]) {
				position++;
			} else {
				position = 0;
			}
		}
		
		if (DEBUG)
			System.out.println("ID");
		
		int id = in.read();
		if (id < 0) {
			throw new EOFException();
		}
		
		if (DEBUG)
			System.out.println("LENGTH");
		
		int length = readInt();
		if (length < 0) {
			throw new PacketException("Invalid data length: " + length);
		}
		if (length > maxDataLength) {
			throw new PacketException("Data length of " + length + " exceeds max of " + maxDataLength);
		}
		
		if (DEBUG)
			System.out.println("DATA");
		
		byte[] data;
		if (length == 0) {
			data = null;
		} else {
			data = new byte[length];
			readFully(data);
		}
		
		if (DEBUG)
			System.out.println("CHECKSUM");
		
		long calculatedChecksum = ((CheckedInputStream) in).getChecksum().getValue();
		long readChecksum = readLong();
		if (readChecksum != calculatedChecksum) {
			throw new PacketException("Checksum failed. Read checksum of " + readChecksum + " does not equal calculated checksum of " + calculatedChecksum + ".");
		}
		
		return new Packet((byte) id, data);
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
