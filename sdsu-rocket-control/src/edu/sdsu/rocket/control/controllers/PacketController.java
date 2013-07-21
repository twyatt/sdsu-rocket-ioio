package edu.sdsu.rocket.control.controllers;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketWriter;

public class PacketController implements PacketListener, PacketWriter {
	
	private static final int BUFFER_SIZE = 1024;
	
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

	private PacketWriter writer;

	public PacketController(PacketWriter writer) {
		this.writer = writer;
	}
	
	private void onIdentRequest(Packet request) {
		sendIdent(App.TAG);
	}
	
	public void sendIdent(String ident) {
		writePacket(Packet.IDENT_RESPONSE, ident.getBytes());
	}

	private void onSensorRequest(Packet request) {
		App.log.i(App.TAG, "onSensorRequest");
	}
	
	public void sendSensorData() {
		Rocket rocket = App.rocketController.getRocket();
		buffer.clear();
		
		buffer.putFloat(rocket.tankPressureLOX.getVoltage());
		buffer.putFloat(rocket.tankPressureEngine.getVoltage());
		buffer.putFloat(rocket.tankPressureEthanol.getVoltage());
		buffer.putFloat(rocket.barometer.pressure);
		buffer.putFloat(rocket.barometer.temperature);
		buffer.put((byte) (rocket.breakWire.isBroken() ? 1 : 0));
		
		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		
		writePacket(Packet.SENSOR_RESPONSE, data);
	}

	private void onDataCollectionRequest(Packet request) {
		if (request.data == null || request.data.length == 0) {
			App.stats.network.packetsDropped++;
			App.log.e(App.TAG, "Invalid data collection request.");
			return;
		}
		App.log.i(App.TAG, "onDataCollectionRequest");
		
		boolean status = false;
		byte state = request.data[0];
		
		switch (state) {
		case Packet.STATE_OFF:
			App.data.disable();
			status = true;
			break;
		case Packet.STATE_ON:
			App.data.enable();
			status = true;
			break;
		case Packet.STATE_RESET:
			status = App.data.reset();
			break;
		}
		
		sendDataCollectionResponse(status);
	}
	
	private void sendDataCollectionResponse(boolean status) {
		 byte data = status ? Packet.STATUS_SUCCESS : Packet.STATUS_FAILURE;
//		 messenger.write(Packet.DATA_COLLECTION_RESPONSE, data);
	}

	protected void onCommandRequest(Packet packet) {
//		objectiveController.command(command.command);
//		
//		if (Network.ABORT_COMMAND.equals(command.command)) {
//			/*
//			 * Allow the abort command to close fuel valves and open tank
//			 * vents no matter what objective is currently active.
//			 */
//			if (App.rocket != null) {
//				App.log.i(App.TAG, "Closing fuel valves and opening tank vents!");
//				App.rocket.servoLOX.open();
//				App.rocket.servoEthanol.open();
//				App.rocket.fuelValve.deactivate();
//			}
//			
//			App.objective.set(Network.LAUNCH_OBJECTIVE);
//		}
	}
	
	/*
	 * PacketWriter interface methods.
	 */
	
	@Override
	public void write(Packet packet) {
		try {
			writer.write(packet);
		} catch (IOException e) {
			App.log.e(App.TAG, "Packet controller failed to write packet.", e);
			e.printStackTrace();
		}
	}

	@Override
	public void writePacket(byte id, byte[] data) {
		try {
			writer.writePacket(id, data);
		} catch (IOException e) {
			App.log.e(App.TAG, "Packet controller failed to write packet.", e);
			e.printStackTrace();
		}
	}
	
	/*
	 * PacketListener interface methods.
	 */
	
	public void onPacketReceived(Packet packet) {
		switch (packet.messageId) {
		case Packet.IDENT_REQUEST:
			onIdentRequest(packet);
			break;
		case Packet.SENSOR_REQUEST:
			onSensorRequest(packet);
			break;
		}
	}

	@Override
	public void onPacketError(Throwable e) {
		App.log.e(App.TAG, "Packet controller packet error.", e);
		e.printStackTrace();
	}

}
