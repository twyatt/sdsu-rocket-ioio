package edu.sdsu.rocket.command.controllers;

import edu.sdsu.rocket.command.models.Rocket;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketWriter;

public class PacketController implements PacketListener {

	public interface PacketControllerListener {
		public void onChange();
	}
	
	private Rocket rocket;
	private PacketControllerListener listener;

	public PacketController(Rocket rocket, PacketWriter writer) {
		this.rocket = rocket;
	}
	
	public PacketController setListener(PacketControllerListener listener) {
		this.listener = listener;
		return this;
	}
	
	private void onIdentResponse(Packet packet) {
		if (packet.data.length > 0) {
			rocket.ident = new String(packet.data);
			onChange();
		}
	}
	
	private void onChange() {
		if (listener != null)
			listener.onChange();
	}

	/*
	 * PacketListener interface methods.
	 */
	
	@Override
	public void onPacketReceived(Packet packet) {
		switch (packet.messageId) {
		case Packet.IDENT_RESPONSE:
			onIdentResponse(packet);
			break;
		}
	}

	@Override
	public void onPacketError(Throwable e) {
		// TODO Auto-generated method stub
		
	}

}
