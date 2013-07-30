package edu.sdsu.rocket.control.controllers;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketWriter;

public class PacketController implements PacketListener {
	
	private static final int BUFFER_SIZE = 1024;
	
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private PacketWriter writer;
	
	public PacketController(PacketWriter writer) {
		this.writer = writer;
	}

	/*
	 * Requests
	 */
	
	private void onIdentRequest(Packet request) {
		sendIdent(App.TAG);
	}
	
	private void onSensorRequest(Packet request) {
		sendSensorData();
	}
	
	private void onDataCollectionRequest(Packet request) {
		if (request.data == null || request.data.length == 0) {
			App.stats.network.packetsDropped.incrementAndGet();
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
	
	/*
	 * Responses
	 */
	
	public void sendIdent(String ident) {
		send(Packet.IDENT_RESPONSE, ident.getBytes());
	}
	
	public void sendSensorData() {
		Rocket rocket = App.rocketController.getRocket();
		buffer.clear();
		
//		buffer.putFloat(rocket.tankPressureLOX.getVoltage());
//		buffer.putFloat(rocket.tankPressureEngine.getVoltage());
//		buffer.putFloat(rocket.tankPressureEthanol.getVoltage());
//		buffer.putFloat(rocket.barometer.pressure);
//		buffer.putFloat(rocket.barometer.temperature);
//		buffer.put((byte) (rocket.breakWire.isBroken() ? 1 : 0));
		
		buffer.putFloat(rocket.accelerometer.getMultiplier());
		buffer.putInt(rocket.accelerometer.getX());
		buffer.putInt(rocket.accelerometer.getY());
		buffer.putInt(rocket.accelerometer.getZ());
		
		buffer.putFloat(rocket.internalAccelerometer.getX());
		buffer.putFloat(rocket.internalAccelerometer.getY());
		buffer.putFloat(rocket.internalAccelerometer.getZ());
		
		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		
		send(Packet.SENSOR_RESPONSE, data);
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
	
	public void send(Packet packet) {
		send(packet.messageId, packet.data);
	}
	
	public void send(byte id, byte[] data) {
		try {
			writer.writePacket(id, data);
		} catch (IOException e) {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Packet controller failed to send packet.", e);
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

}
