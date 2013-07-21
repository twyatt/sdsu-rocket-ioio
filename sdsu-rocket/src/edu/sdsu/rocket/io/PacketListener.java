package edu.sdsu.rocket.io;

public interface PacketListener {
	
	public void onPacketReceived(Packet packet);
	public void onPacketError(Throwable e);
	
}
