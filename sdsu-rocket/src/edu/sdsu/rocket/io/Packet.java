package edu.sdsu.rocket.io;

public class Packet {

	public byte messageId;
	public byte[] data;
	
	@Override
	public String toString() {
		return super.toString() + ": id=" + messageId + ", data.length=" + data.length;
	}
	
}
